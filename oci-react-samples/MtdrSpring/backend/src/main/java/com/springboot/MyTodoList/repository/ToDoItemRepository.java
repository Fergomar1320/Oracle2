package com.springboot.MyTodoList.repository;


import com.springboot.MyTodoList.model.ToDoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

import javax.transaction.Transactional;

@Repository
@Transactional
@EnableTransactionManagement
public interface ToDoItemRepository extends JpaRepository<ToDoItem,Integer> {
  
    //findTasksByDeveloperName
    @Query("SELECT ti.ITEM_ID, ti.ITEM_DESCRIPTION, ti.ITEM_STATUS, ti.ITEM_DEADLINE " +
    "FROM TODOUSER.TODOITEM ti INNER JOIN TODOUSER.ORACLEUSER ou ON ti.USER_ID = ou.USER_ID WHERE ou.USER_NAME =  ':developerName'")
    List<ToDoItem> findTasksByDeveloperName(@Param("developerName") String developerName);

    //findTasksByUserId
    @Query("SELECT ti.ITEM_ID, ti.ITEM_DESCRIPTION, ti.ITEM_STATUS, ti.ITEM_DEADLINE " +
    "FROM TODOUSER.TODOITEM ti INNER JOIN TODOUSER.ORACLEUSER ou ON ti.USER_ID = ou.USER_ID WHERE ou.USER_ID = :userId")
    List<ToDoItem> findTasksByUserId(@Param("userId") int userId);
}
