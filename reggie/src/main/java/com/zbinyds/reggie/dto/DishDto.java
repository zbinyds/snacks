package com.zbinyds.reggie.dto;


import com.zbinyds.reggie.pojo.Dish;
import com.zbinyds.reggie.pojo.DishFlavor;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DishDto extends Dish {

    private List<DishFlavor> flavors = new ArrayList<>();

    private String categoryName;

    private Integer copies;
}
