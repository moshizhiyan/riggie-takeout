package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐同时保存菜品和套餐关系
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息
        super.save(setmealDto);

        //保存关联信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> setmealDish.setSetmealId(setmealDto.getId()));
        setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐的同时删除对应的套餐和菜品关联信息
     * @param ids
     */
    @Override
    @Transactional
    public void deleteWithDish(List<Long> ids) {

        //先判断能否删除（是否起售）
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.in(Setmeal::getId, ids).eq(Setmeal::getStatus, 1);

        //如果能删，删除套餐表数据及套餐菜品表数据
        if(super.count(queryWrapper) == 0){
            super.removeByIds(ids);
            //删除关联信息
            LambdaQueryWrapper<SetmealDish> setmealDishLambdaQueryWrapper = new LambdaQueryWrapper<>();
            setmealDishLambdaQueryWrapper.in(SetmealDish::getSetmealId, ids);
            setmealDishService.remove(setmealDishLambdaQueryWrapper);
            return;
        }

        //如果不能删，抛出业务异常
        throw new CustomException("启售状态的套餐不能删除");
    }

    /**
     * 回显套餐信息及其对应菜品
     * @param id
     * @return
     */
    @Override
    public SetmealDto getByIdWithDish(Long id) {
        //查询基本套餐知识
        Setmeal setmeal = super.getById(id);

        //查询菜品信息
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId, id);
        List<SetmealDish> list = setmealDishService.list(queryWrapper);

        //将菜品信息封装到SetmealDto中
        SetmealDto setmealDto = new SetmealDto();
        BeanUtils.copyProperties(setmeal, setmealDto);
        setmealDto.setSetmealDishes(list);

        return setmealDto;
    }

    /**
     * 改变菜品的启售停售状态
     * @param ids
     * @return
     */
    @Override
    @Transactional
    public int changeStatus(List<Long> ids, Integer status) {
        //起售
        if (status == 1){

            //判断是否有菜品处于停售状态
            LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper();
            queryWrapper.in(SetmealDish::getSetmealId, ids);
            //获取所有套餐关联信息
            List<SetmealDish> setmealDishes = setmealDishService.list(queryWrapper);
            setmealDishes.forEach(setmealDish -> {
                LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
                dishLambdaQueryWrapper.eq(Dish::getId, setmealDish.getDishId());
                Dish dish = dishService.getOne(dishLambdaQueryWrapper);
                //菜品处于停售状态，抛出异常
                if (dish.getStatus() == 0) throw new CustomException("套餐包含未启售菜品，无法启售");
            });

            //可以启售套餐
            ids.forEach(id ->{
                //Setmeal setmeal = new Setmeal();
                //setmeal.setId(id);
                Setmeal setmeal = super.getById(id);
                setmeal.setStatus(1);
                super.updateById(setmeal);
            });

            return status;
        }

        //停售
        ids.forEach(id ->{
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(0);
            super.updateById(setmeal);
        });

        return status;
    }

    /**
     * 跟新套餐信息及其对应菜品关系
     * @param setmealDto
     * @return
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDto setmealDto) {
        //更新套餐基本信息
        super.updateById(setmealDto);

        //删除原先菜品对应关系
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SetmealDish::getSetmealId, setmealDto.getId());
        setmealDishService.remove(queryWrapper);

        //插入新的菜品对应关系
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //将SetmealDish的Id设置为空，否则会因为逻辑删除无法插入
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setId(null);
            setmealDish.setSetmealId(setmealDto.getId());
        });
        setmealDishService.saveBatch(setmealDishes);

    }
}
