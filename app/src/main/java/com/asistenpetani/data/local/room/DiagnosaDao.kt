package com.asistenpetani.data.local.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosaDao {
    @Query("SELECT * FROM farmer_profile WHERE uid = 'default_user' LIMIT 1")
    fun getProfile(): Flow<FarmerProfile?>

    @Query("SELECT * FROM farmer_profile WHERE uid = 'default_user' LIMIT 1")
    suspend fun getProfileSync(): FarmerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: FarmerProfile)

    @Query("DELETE FROM farmer_profile")
    suspend fun deleteProfile()

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getChatMessages(): Flow<List<ChatMessage>>
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    suspend fun getChatMessagesSync(): List<ChatMessage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    @Query("DELETE FROM chat_messages WHERE text = :text")
    suspend fun deleteChatMessageByText(text: String)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteChatMessageBySessionId(sessionId: String)

    @Query("SELECT * FROM planting_schedules ORDER BY plantingDate ASC")
    fun getPlantingSchedules(): Flow<List<PlantingSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlantingSchedule(schedule: PlantingSchedule)

    @Delete
    suspend fun deletePlantingSchedule(schedule: PlantingSchedule)

    @Query("DELETE FROM planting_schedules")
    suspend fun clearPlantingSchedules()
}
