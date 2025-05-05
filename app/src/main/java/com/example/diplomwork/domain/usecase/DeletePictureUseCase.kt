package com.example.diplomwork.domain.usecase

import com.example.diplomwork.data.repos.PictureRepository
import javax.inject.Inject

class DeletePictureUseCase @Inject constructor(
    private val pictureRepository: PictureRepository
) {
    suspend fun delete(pictureId: Long): Result<Unit> {
        return pictureRepository.deletePicture(pictureId)
    }
}