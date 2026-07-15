package cn.ynu.campus.relife.item.controller;

import cn.ynu.campus.relife.common.core.result.ApiResponse;
import cn.ynu.campus.relife.item.dto.CategoryVO;
import cn.ynu.campus.relife.item.service.CategoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryVO>> list(@RequestParam(required = false, defaultValue = "0") Long parentId) {
        return ApiResponse.ok(categoryService.listByParentId(parentId));
    }

    @GetMapping("/{categoryId}")
    public ApiResponse<CategoryVO> detail(@PathVariable Long categoryId) {
        return ApiResponse.ok(categoryService.getById(categoryId));
    }
}
