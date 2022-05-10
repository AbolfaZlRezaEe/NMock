package me.abolfazl.nmock.utils.response.exceptions

import javax.annotation.Nonnull
import javax.annotation.Nullable

data class NMockException(
    @Nullable val message: String? = null,
    @Nonnull @ExceptionType val type: String,
)