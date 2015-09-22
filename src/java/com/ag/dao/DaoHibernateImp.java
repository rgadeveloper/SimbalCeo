/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ag.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbcp.BasicDataSource;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author 85154220
 */

@Service("DaoHibernate")
public class DaoHibernateImp  extends HibernateDaoSupport implements Dao { 
    
    @Autowired
    @Qualifier ("dataSource")
    private BasicDataSource dataSource;
    
    @Autowired 
    public DaoHibernateImp(@Qualifier("sessionFactory") SessionFactory sessionFactory) throws SQLException { 
        this.setSessionFactory(sessionFactory); 
    }

    @Transactional
    public void persist(Object entity) {
        getHibernateTemplate().saveOrUpdate(entity);
    }

    @Transactional
    public void persist(Object[] entities) {
        for (int i = 0; i < entities.length; i++) {
            persist(entities[i]);
        }
    }

    @Transactional(readOnly = true)
    public <T> T load(Class<T> entityClass, Serializable id) {
        final T entity = (T)getHibernateTemplate().load(entityClass, id);
        return entity;
    }

    @Transactional(readOnly = true)
    public <T> List<T> find(String hql) {
        final List<T> entities = getHibernateTemplate().find(hql);
        return entities;
    }

    @Transactional(readOnly = true)
    public <T> List<T> findByNamedQuery(String namedQuery) {
        final List<T> entities = getHibernateTemplate().findByNamedQuery(namedQuery);
        return entities;
    }

    @Transactional(readOnly = true)
    public <T> List<T> findByNamedQuery(String namedQuery,Object ... values) {
        final List<T> entities = getHibernateTemplate().findByNamedQuery(namedQuery,values);
        return entities;
    }

    @Transactional(readOnly = true)
    public <T> T findByNamedQueryObject(String namedQuery) {
        final List<T> entities = getHibernateTemplate().findByNamedQuery(namedQuery);
        if(entities.size()> 0)
            return entities.get(0);
        return null;
    }

    @Transactional(readOnly = true)
    public <T> T findByNamedQueryObject(String namedQuery,Object ... values) {
        final List<T> entities = getHibernateTemplate().findByNamedQuery(namedQuery,values);

        if(entities.size()> 0)
            return entities.get(0);
        return null;
    }

    @Transactional(readOnly = true)
    public <T>  T findObject(String hql) {        
        List<T> entities = find(hql);
        if(entities.size()> 0)
            return entities.get(0);
        return null;
    }

    @Transactional(readOnly = true)
    public <T> List<T> find(Class<T> entityClass) {
        //getHibernateTemplate().getSessionFactory().openSession();
        final List<T> entities = getHibernateTemplate().loadAll(entityClass);
        //getHibernateTemplate().getSessionFactory().close();
        return entities;
    }
    
    @Override
    public Connection getConnection(){    
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DaoHibernateImp.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    @Transactional
    public void delete(Object entity) {
        getHibernateTemplate().delete(entity);
    }

       
     /**
     * Gestion las onexiones sql
     * @autor <b>Jose Mejia</b>
     * @see DaoHibernateImp
     * @since 27/11/2014
     * @param sql
     * @param unElemento
     * @return 
     */
     @Override
    public List executeQuerie(final String sql, final boolean unElemento) {
        //getHibernateTemplate();
        List ResultList = (List) getHibernateTemplate().execute(new HibernateCallback() {
            @Override
            public Object doInHibernate(Session sn) throws HibernateException, SQLException {
                List list = new ArrayList();
                //Ejecuta SQL
                try {
                    Query sq = sn.createSQLQuery(sql);
                    if (unElemento) {
                        list.add(sq.setMaxResults(1).uniqueResult());
                    } else {
                        list.addAll(sq.list());
                    }
                } catch (Exception ee) {
                    ee.printStackTrace();
                }
                return list;
            }
        });

        return ResultList;
    }

}
