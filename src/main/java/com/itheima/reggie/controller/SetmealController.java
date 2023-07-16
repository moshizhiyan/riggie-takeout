package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("保存套餐成功");
    }

    /**
     * 分页查询菜品
     * @param pageSize
     * @param page
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int pageSize, int page, String name){
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);

        //Setmeal中的信息不够需要借助SetmealDto
        Page<SetmealDto> setmealDtoPage = new Page<>();

        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Setmeal::getName, name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setmealService.page(pageInfo, queryWrapper);

        //将查询出的数据拷贝给Page<setmealDto>
        BeanUtils.copyProperties(pageInfo, setmealDtoPage, "records");

        //处理records。为其加上CategoryName
        List<Setmeal> records = pageInfo.getRecords();
        List<SetmealDto> list = records.stream().map(setmeal -> {
            //查询categoryName
            Long categoryId = setmeal.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //封装数据到setmealDto
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(setmeal, setmealDto);
            setmealDto.setCategoryName(categoryName);

            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(list);

        return R.success(setmealDtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @Transactional
    public R<String> delete(@RequestParam List<Long> ids){
        setmealService.deleteWithDish(ids);

        return R.success("删除成功");
    }

    /**
     * 改变套餐启售停售状态
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@RequestParam List<Long> ids, @PathVariable int status){
        int result = setmealService.changeStatus(ids, status);
        if (result == 1) return R.success("起售成功");
        return R.success("停售成功");
    }

    /**
     * 回显套餐信息
     * @param id
     * @return
     */
    @GetMapping("{id}")
    public R<SetmealDto> get(@PathVariable Long id){

        SetmealDto setmealDto = setmealService.getByIdWithDish(id);

        return R.success(setmealDto);
    }

    /**
     * 根据条件查询套餐
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId() != null, Setmeal::getCategoryId, setmeal.getCategoryId());
        queryWrapper.eq(Setmeal::getStatus, 1);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 跟新套餐信息及其对应菜品关系
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 获取菜列表
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    public R<List<Dish>> dish(@PathVariable Long id){
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
        List<Dish> dishList = setmealDishes.stream().map(setmealDish -> {
            Dish dish = dishService.getById(setmealDish.getDishId());
            return dish;
        }).collect(Collectors.toList());
        return R.success(dishList);
    }
}
