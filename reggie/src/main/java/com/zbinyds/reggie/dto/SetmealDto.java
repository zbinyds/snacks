package com.zbinyds.reggie.dto;

import com.zbinyds.reggie.pojo.Setmeal;
import com.zbinyds.reggie.pojo.SetmealDish;
import lombok.Data;
import java.util.List;

@Data
public class SetmealDto extends Setmeal {

    private List<SetmealDish> setmealDishes;

    private String categoryName;
}
