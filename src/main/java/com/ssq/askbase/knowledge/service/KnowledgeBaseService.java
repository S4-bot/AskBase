package com.ssq.askbase.knowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ssq.askbase.knowledge.Vo.KnowledgeBaseVo;
import com.ssq.askbase.knowledge.dto.KnowledgeCreateRequest;
import com.ssq.askbase.knowledge.dto.KnowledgeUpdateRequest;
import com.ssq.askbase.knowledge.entity.KnowledgeBase;

import java.util.List;

public interface KnowledgeBaseService extends IService<KnowledgeBase> {


    KnowledgeBaseVo create(KnowledgeCreateRequest request);

    List<KnowledgeBaseVo> selectById();

    KnowledgeBaseVo getknowledgeBase(Long id);

    KnowledgeBaseVo upadteknowledgeBase(Long id, KnowledgeUpdateRequest  request);

    void deleteById(Long id);

}
