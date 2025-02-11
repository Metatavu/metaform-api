package fi.metatavu.metaform.server.controllers

abstract class AbstractMetaformResourceController<T> {
    abstract fun delete(entity: T)
}