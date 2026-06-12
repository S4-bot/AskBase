package com.ssq.askbase.knowledge.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ssq.askbase.common.enums.ErrorCode;
import com.ssq.askbase.common.exception.BusinessException;
import com.ssq.askbase.common.util.UserContextHolder;
import com.ssq.askbase.knowledge.Vo.KnowledgeBaseVo;
import com.ssq.askbase.knowledge.dto.KnowledgeCreateRequest;
import com.ssq.askbase.knowledge.dto.KnowledgeUpdateRequest;
import com.ssq.askbase.knowledge.entity.KnowledgeBase;
import com.ssq.askbase.knowledge.mapper.KnowledgeBaseMapper;
import com.ssq.askbase.knowledge.service.KnowledgeBaseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;


@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements KnowledgeBaseService {




    @Override
    @Transactional
    public KnowledgeBaseVo create(KnowledgeCreateRequest request) {

        Long userId = isUserId();


        boolean exists = this.lambdaQuery()
                .eq(KnowledgeBase::getUserId, userId)
                .eq(KnowledgeBase::getName, request.getName())
                .eq(KnowledgeBase::getDeleted, false)
                .exists();

        if (exists) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        }

        LocalDateTime now = LocalDateTime.now();
        KnowledgeBase knowledge = KnowledgeBase.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .deleted(false)
                .createdAt(now)
                .updatedAt(now)
                .build();

        if(!this.save(knowledge)){
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        }

        return BeanUtil.toBean(knowledge, KnowledgeBaseVo.class);
    }

    @Override
    public List<KnowledgeBaseVo> selectById() {
        Long userId = isUserId();
        List<KnowledgeBase> list = this.lambdaQuery()
                .eq(KnowledgeBase::getUserId,userId )
                .eq(KnowledgeBase::getDeleted, false)
                .orderByAsc(KnowledgeBase::getId)
                .list();
        if(list == null){
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        }


        return BeanUtil.copyToList(list, KnowledgeBaseVo.class);
    }

    @Override
    public KnowledgeBaseVo getknowledgeBase(Long id) {
        Long userId = isUserId();
        KnowledgeBase knowledgeBase = this.lambdaQuery()
                .eq(KnowledgeBase::getId, id)
                .eq(KnowledgeBase::getUserId, userId)
                .eq(KnowledgeBase::getDeleted, false)
                .one();
        if(knowledgeBase == null){
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        return BeanUtil.toBean(knowledgeBase, KnowledgeBaseVo.class);
    }

    @Override
    @Transactional
    public KnowledgeBaseVo upadteknowledgeBase(Long id, KnowledgeUpdateRequest request) {
        Long userId = isUserId();
        
        // 先查询知识库是否存在且属于当前用户
        KnowledgeBase knowledgeBase = this.lambdaQuery()
                .eq(KnowledgeBase::getId, id)
                .eq(KnowledgeBase::getUserId, userId)
                .eq(KnowledgeBase::getDeleted, false)
                .one();
        
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 如果修改了名称，需要检查名称唯一性
        if (request.getName() != null && !request.getName().equals(knowledgeBase.getName())) {
            boolean exists = this.lambdaQuery()
                    .eq(KnowledgeBase::getUserId, userId)
                    .eq(KnowledgeBase::getName, request.getName())
                    .eq(KnowledgeBase::getDeleted, false)
                    .ne(KnowledgeBase::getId, id)
                    .exists();

            if (exists) {
                throw new BusinessException(ErrorCode.BUSINESS_ERROR);
            }
            knowledgeBase.setName(request.getName());
        }

        // 如果修改了描述，更新描述
        if (request.getDescription() != null) {
            knowledgeBase.setDescription(request.getDescription());
        }

        // 更新时间
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        
        // 执行更新
        if (!this.updateById(knowledgeBase)) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        }

        return BeanUtil.toBean(knowledgeBase, KnowledgeBaseVo.class);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Long userId = isUserId();
        
        // 先查询知识库是否存在且属于当前用户
        KnowledgeBase knowledgeBase = this.lambdaQuery()
                .eq(KnowledgeBase::getId, id)
                .eq(KnowledgeBase::getUserId, userId)
                .eq(KnowledgeBase::getDeleted, false)
                .one();
        
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 执行软删除
        boolean success = this.lambdaUpdate()
                .eq(KnowledgeBase::getId, id)
                .eq(KnowledgeBase::getUserId, userId)
                .set(KnowledgeBase::getDeleted, true)
                .set(KnowledgeBase::getUpdatedAt, LocalDateTime.now())
                .update();
        
        if (!success) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR);
        }
    }

    public  static Long isUserId(){
        Long userId = UserContextHolder.getUserId();
        if(userId == null){
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return userId;
    }

}
