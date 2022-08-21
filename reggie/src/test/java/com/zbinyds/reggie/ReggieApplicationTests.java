package com.zbinyds.reggie;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zbinyds.reggie.mapper.DishMapper;
import com.zbinyds.reggie.pojo.Dish;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ReggieApplicationTests {

    @Autowired(required = false)
    private DishMapper dishMapper;

    @Test
    void contextLoads() {
        Page<Dish> dishPage = new Page<>(1,5);
        dishMapper.selectPageVo(dishPage,null);
        System.out.println(dishPage);
    }

}
