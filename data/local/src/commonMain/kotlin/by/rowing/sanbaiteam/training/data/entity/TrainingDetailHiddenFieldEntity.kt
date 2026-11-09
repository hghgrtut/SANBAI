package by.rowing.sanbaiteam.training.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import by.rowing.sanbaiteam.core.annotation.AllowDetektPublic
import by.rowing.sanbaiteam.training.data.local.TrainingDetailHiddenFieldDao

@AllowDetektPublic
@Entity(tableName = TrainingDetailHiddenFieldDao.TABLE_TRAINING_DETAIL_HIDDEN_FIELD)
data class TrainingDetailHiddenFieldEntity(@PrimaryKey val field: String)