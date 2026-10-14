package by.rowing.sanbaiteam.training.data.trainingimport

internal class EmptyTrainingFileReader : TrainingFileReader {

    override suspend fun readFiles(handles: List<String>): List<PickedImportFile> = emptyList()
}
