package com.example.diplomwork.domain.usecase

import com.example.diplomwork.data.repos.SpotRepository
import javax.inject.Inject

class DeletePictureUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend fun delete(pictureId: Long): Result<Unit> {
        return spotRepository.deletePicture(pictureId)
    }
}