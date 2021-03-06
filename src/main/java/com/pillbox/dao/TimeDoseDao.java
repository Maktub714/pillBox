package com.pillbox.dao;

import com.pillbox.po.TimeDose;

/**
 * User:  maktub
 * Date:   16/5/11 下午3:57
 */
public interface TimeDoseDao {

    void save(TimeDose dose);

    void update(TimeDose dose);

    void delete(TimeDose dose);

    void deleteById(Long id);

}
