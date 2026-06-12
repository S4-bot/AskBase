package com.ssq.askbase.knowledge.controller;

import com.ssq.askbase.common.response.ApiResponse;
import com.ssq.askbase.knowledge.Vo.KnowledgeBaseVo;
import com.ssq.askbase.knowledge.dto.KnowledgeCreateRequest;
import com.ssq.askbase.knowledge.dto.KnowledgeUpdateRequest;
import com.ssq.askbase.knowledge.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping({ "/api/knowledge"})
public class KnowledgeBaseController {

    private final KnowledgeBaseService knowledgeBaseService;

    @PostMapping
    public ApiResponse<KnowledgeBaseVo> create(@Valid @RequestBody KnowledgeCreateRequest request){
        return ApiResponse.success(knowledgeBaseService.create( request));
    }

    @GetMapping
    public ApiResponse<List<KnowledgeBaseVo>> list(){
        return ApiResponse.success(knowledgeBaseService.selectById());
    }

    @PutMapping("/{id}")
    public ApiResponse<KnowledgeBaseVo> update(@PathVariable Long id, @Valid @RequestBody KnowledgeUpdateRequest request){
        return ApiResponse.success(knowledgeBaseService.upadteknowledgeBase(id, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<KnowledgeBaseVo> get(@PathVariable Long id){
        return ApiResponse.success(knowledgeBaseService.getknowledgeBase(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id){
        knowledgeBaseService.deleteById(id);
        return ApiResponse.success();
    }

}
