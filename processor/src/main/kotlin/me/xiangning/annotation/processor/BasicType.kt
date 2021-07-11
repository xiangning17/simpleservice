package me.xiangning.annotation.processor

import me.xiangning.annotation.processor.AidlUtils.asType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Types

/**
 * Created by xiangning on 2021/7/11.
 */
class BasicType(private val types: Types) {
    private var stringType: TypeMirror = String::class.java.asType()
    private var listType: TypeMirror = List::class.java.asType()
    private var mapType: TypeMirror = Map::class.java.asType()
    private var basicTypes: List<TypeMirror> = mutableListOf(
        stringType,
        types.getPrimitiveType(TypeKind.BOOLEAN),
        types.getPrimitiveType(TypeKind.BYTE),
        types.getPrimitiveType(TypeKind.SHORT),
        types.getPrimitiveType(TypeKind.INT),
        types.getPrimitiveType(TypeKind.LONG),
        types.getPrimitiveType(TypeKind.CHAR),
        types.getPrimitiveType(TypeKind.FLOAT),
        types.getPrimitiveType(TypeKind.DOUBLE),
    ).apply {
        addAll(this.map { types.getArrayType(it) })
    }

    fun isBasicType(type: String): Boolean {
        return type == "void" || basicTypes.any {
            it.toString() == type
        }
    }

}