package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 分页查询菜品
     * @param pageSize
     * @param page
     * @return
     */
    @RequestMapping("/page")
    public R<Page> page(int pageSize, int page, String name){
        Page<Dish> pageInfo = new Page<>(page, pageSize);

        //Dish中的信息不够需要再借助DishDto
        Page<DishDto> dishDtoPage = new Page<>();

        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(name), Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageInfo, queryWrapper);

        //将查询出的数据拷贝给Page<DishDto>
        BeanUtils.copyProperties(pageInfo, dishDtoPage, "records");

        //处理records。为其加上CategoryName
        List<Dish> records = pageInfo.getRecords();
        List<DishDto> list = records.stream().map(dish -> {
            //查询categoryName
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();

            //封装数据到DishDto
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            dishDto.setCategoryName(categoryName);

            return dishDto;
        }).collect(Collectors.toList());

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 保存菜品信息
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        return R.success("添加成功");
    }

    /**
     * 根据id查询菜品信息和对应属性信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品信息
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.updateWithFlavor(dishDto);
        return R.success("修改成功");
    }

    /**
     * 根据id删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){

        //dishService.removeByIds(ids);
        //要判断菜品是否包含在套餐内
        dishService.deleteJudgeSetmeal(ids);

        return R.success("删除成功");
    }

    /**
     * 改变菜品售卖状态
     * @param ids
     * @param status
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@RequestParam List<Long> ids, @PathVariable int status){
        //起售
        if (status == 1){
            ids.forEach(id ->{
                Dish dish = new Dish();
                dish.setId(id);
                dish.setStatus(1);
                dishService.updateById(dish);
            });
            return R.success("起售成功");
        }

        //停售
        ids.forEach(id ->{
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(0);
            dishService.updateById(dish);

            //获取包含菜品的套餐id
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(SetmealDish::getDishId, ids);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            HashSet<Long> setMealIds = new HashSet<>();
            list.forEach(setmealDish -> {
                Long setmealId = setmealDish.getSetmealId();
                setMealIds.add(setmealId);
            });
            log.info(setMealIds.toString());

            //将包含菜品的套餐状态改为停售
            setmealService.changeStatus(setMealIds.stream().collect(Collectors.toList()), 0);
        });
        return R.success("停售成功");
    }

    /**
     * 根据条件查询菜品
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> list(Dish dish){
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null, Dish::getCategoryId, dish.getCategoryId());
        //筛选只查询起售
        queryWrapper.eq(Dish::getStatus, 1);

        queryWrapper.like(StringUtils.isNotEmpty(dish.getName()), Dish::getName, dish.getName());
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);
        List<DishDto> dishDtos = list.stream().map(e -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(e, dishDto);
            //查询菜品口味信息
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper();
            wrapper.eq(DishFlavor::getDishId, e.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);

            //查询categoryName
            Long categoryId = dish.getCategoryId();
            Category category = categoryService.getById(categoryId);
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);

            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());
        return R.success(dishDtos);
    }
}
