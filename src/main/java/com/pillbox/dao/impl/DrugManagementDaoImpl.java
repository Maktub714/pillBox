package com.pillbox.dao.impl;

import com.pillbox.dao.DrugManagementDao;
import com.pillbox.po.DrugManagement;
import com.pillbox.po.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User:  maktub
 * Date:   16/5/11 下午12:06
 */
@Repository
public class DrugManagementDaoImpl implements DrugManagementDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(DrugManagement drugManagement) {
        this.sessionFactory.openSession().save(drugManagement);
    }

    @Override
    public List<DrugManagement> selectByOpenId(User user) {
        return this.sessionFactory.openSession().createQuery("from DrugManagement where user = :user").setParameter("user", user).list();
    }
}