package com.example.tmusic.study.data

import androidx.room.*
import androidx.room.Dao
import androidx.room.Insert

@Dao
interface PlanDao {
    @Insert
    suspend fun insertPlan(plan: PlanEntity)

    @Query("DELETE FROM studyPlan WHERE id = :id")
    suspend fun deletePlan(id: Int)

    @Query("UPDATE studyPlan SET isFinished = :isFinished WHERE id = :id ")
    suspend fun updatePlan(id: Int, isFinished: Boolean)

    @Query("SELECT * FROM studyPlan WHERE date = :date")
    suspend fun queryPlanByDate(date: String): List<PlanEntity>

    @Query("SELECT * FROM studyPlan WHERE id = :id")
    suspend fun queryPlanById(id: Int): PlanEntity

}