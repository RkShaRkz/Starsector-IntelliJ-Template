package data.script

import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.net.URL
import java.net.URLClassLoader

object ReflectionUtils {

    private val fieldClass = Class.forName("java.lang.reflect.Field", false, Class::class.java.classLoader)
    private val setFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "set", MethodType.methodType(Void.TYPE, Any::class.java, Any::class.java))
    private val getFieldHandle = MethodHandles.lookup().findVirtual(fieldClass, "get", MethodType.methodType(Any::class.java, Any::class.java))
    private val getFieldNameHandle = MethodHandles.lookup().findVirtual(fieldClass, "getName", MethodType.methodType(String::class.java))
    private val setFieldAccessibleHandle = MethodHandles.lookup().findVirtual(fieldClass, "setAccessible", MethodType.methodType(Void.TYPE, Boolean::class.javaPrimitiveType))

    private val methodClass = Class.forName("java.lang.reflect.Method", false, Class::class.java.classLoader)
    private val getMethodNameHandle = MethodHandles.lookup().findVirtual(methodClass, "getName", MethodType.methodType(String::class.java))
    private val invokeMethodHandle = MethodHandles.lookup().findVirtual(methodClass, "invoke", MethodType.methodType(Any::class.java, Any::class.java, Array<Any>::class.java))

    //RKZ
    private val getDeclaredFieldsHandle = MethodHandles.lookup().findVirtual(fieldClass, "getDeclaredFields", MethodType.methodType(Field::class.java, String::class.java))
    private val getModifiersHandle = MethodHandles.lookup().findVirtual(fieldClass, "getModifiers", MethodType.methodType(Int::class.java))
    private val setIntHandle = MethodHandles.lookup().findVirtual(fieldClass, "setInt", MethodType.methodType(Any::class.java, Int::class.java))
    private val setFloatHandle = MethodHandles.lookup().findVirtual(fieldClass, "setFloat", MethodType.methodType(Any::class.java, Float::class.java))

    fun modifyFinalField(fieldName:String, clazz: Class<*>, newValue: Float) {
        ConstraintChangerModPlugin.logger.info("[SHARK] ----> modifyFinalField(fieldName=$fieldName, clazz=$clazz, newValue=$newValue)")
        setOrUnsetFinal(fieldName, clazz, false);
        setFloatHandle.invoke(clazz.getField(fieldName), newValue)
        setOrUnsetFinal(fieldName, clazz, true);
        ConstraintChangerModPlugin.logger.info("[SHARK] <---- modifyFinalField()")
    }

    /**
     * Method that sets or unsets the FINAL modifier on some field.
     * @param fieldName field name on which to operate
     * @param clazz class on which to operate on
     * @param setOrUnset [boolean] use 'true' to set or 'false' to unset
     */
    fun setOrUnsetFinal(fieldName: String, clazz: Class<*>, setOrUnset: Boolean) {
        ConstraintChangerModPlugin.logger.info("[SHARK] ----> setOrUnsetFinal(fieldName=$fieldName, clazz=$clazz, setOrUnset=$setOrUnset)")
        var field: Any? = null
        try {
            field = clazz.getField(fieldName)
        } catch (e: Throwable) {
            try {
                field = clazz.getDeclaredField(fieldName)
            } catch (e: Throwable) {
                ConstraintChangerModPlugin.logger.error("[SHARK] Could not get field named $fieldName from class $clazz")
            }
        }

        setFieldAccessibleHandle.invoke(field, true)
        val fieldsModifiersField = getDeclaredFieldsHandle.invokeWithArguments(field, "modifiers")
        setFieldAccessibleHandle.invoke(fieldsModifiersField, true)
        val modifiers = getModifiersHandle.invoke(field) as Int

        setIntHandle.invoke(fieldsModifiersField, modifiers and if (setOrUnset) {
            Modifier.FINAL
        } else {
            Modifier.FINAL.inv()
        })
    }


    /**
     * Method that sets or unsets the FINAL modifier on some field.
     * @param fieldName field name on which to operate
     * @param instanceToModify instance on which to operate on
     * @param setOrUnset [boolean] use 'true' to set or 'false' to unset
     */
    fun setOrUnsetFinal(fieldName: String, instanceToModify: Any?, setOrUnset: Boolean) {
        var field: Any? = null
        try {
//            field = instanceToModify?.javaClass.getField(fieldName)
            instanceToModify?.let {
                field = it.javaClass.getField(fieldName)
            }
        } catch (e: Throwable) {
            try {
//                field = instanceToModify?.javaClass.getDeclaredField(fieldName)
                instanceToModify?.let {
                    field = it.javaClass.getDeclaredField(fieldName)
                }
            } catch (e: Throwable) {
                ConstraintChangerModPlugin.logger.error("[SHARK] Could not get field named $fieldName from instance $instanceToModify")
            }
        }

        setFieldAccessibleHandle.invoke(field, true)
        val fieldsModifiersField = getDeclaredFieldsHandle.invokeWithArguments(field, "modifiers")
        setFieldAccessibleHandle.invoke(fieldsModifiersField, true)
        val modifiers = getModifiersHandle.invoke(field) as Int

        setIntHandle.invoke(fieldsModifiersField, modifiers and if (setOrUnset) {
            Modifier.FINAL
        } else {
            Modifier.FINAL.inv()
        })//fieldsModifiersField.setInt
    }

    fun set(fieldName: String, instanceToModify: Any, newValue: Any?) {
        var field: Any? = null
        try {
            field = instanceToModify.javaClass.getField(fieldName)
        } catch (e: Throwable) {
            try {
                field = instanceToModify.javaClass.getDeclaredField(fieldName)
            } catch (e: Throwable) {
                ConstraintChangerModPlugin.logger.error("[SHARK] Could not set field named $fieldName from instance $instanceToModify to new value $newValue")
            }
        }

        setFieldAccessibleHandle.invoke(field, true)
        setFieldHandle.invoke(field, instanceToModify, newValue)
    }

    fun get(fieldName: String, instanceToGetFrom: Any): Any? {
        var field: Any? = null
        try {
            field = instanceToGetFrom.javaClass.getField(fieldName)
        } catch (e: Throwable) {
            try {
                field = instanceToGetFrom.javaClass.getDeclaredField(fieldName)
            } catch (e: Throwable) {
                ConstraintChangerModPlugin.logger.error("[SHARK] Could not get field named $fieldName from instance $instanceToGetFrom")
            }
        }

        setFieldAccessibleHandle.invoke(field, true)
        return getFieldHandle.invoke(field, instanceToGetFrom)
    }

    fun hasMethodOfName(name: String, instance: Any, contains: Boolean = false): Boolean {
        val instancesOfMethods: Array<out Any> = instance.javaClass.getDeclaredMethods()

        if (!contains) {
            return instancesOfMethods.any { getMethodNameHandle.invoke(it) == name }
        } else {
            return instancesOfMethods.any { (getMethodNameHandle.invoke(it) as String).contains(name) }
        }
    }

    fun hasVariableOfName(name: String, instance: Any): Boolean {

        val instancesOfFields: Array<out Any> = instance.javaClass.getDeclaredFields()
        return instancesOfFields.any { getFieldNameHandle.invoke(it) == name }
    }

    fun instantiate(clazz: Class<*>, vararg arguments: Any?): Any? {
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it!!::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        val constructorHandle = MethodHandles.lookup().findConstructor(clazz, methodType)
        val instance = constructorHandle.invokeWithArguments(arguments.toList())

        return instance
    }

    fun invoke(methodName: String, instance: Any, vararg arguments: Any?, declared: Boolean = false): Any? {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        if (!declared) {
            method = clazz.getMethod(methodName, *methodType.parameterArray())
        } else {
            method = clazz.getDeclaredMethod(methodName, *methodType.parameterArray())
        }

        return invokeMethodHandle.invoke(method, instance, arguments)
    }

    fun getField(fieldName: String, instanceToGetFrom: Any): ReflectedField? {
        var field: Any? = null
        try {
            field = instanceToGetFrom.javaClass.getField(fieldName)
        } catch (e: Throwable) {
            try {
                field = instanceToGetFrom.javaClass.getDeclaredField(fieldName)
            } catch (e: Throwable) {
            }
        }

        if (field == null) return null

        return ReflectedField(field)
    }

    fun getMethod(methodName: String, instance: Any, vararg arguments: Any?): ReflectedMethod? {
        var method: Any? = null

        val clazz = instance.javaClass
        val args = arguments.map { it!!::class.javaPrimitiveType ?: it::class.java }
        val methodType = MethodType.methodType(Void.TYPE, args)

        try {
            method = clazz.getMethod(methodName, *methodType.parameterArray())
        } catch (e: Throwable) {
            try {
                method = clazz.getDeclaredMethod(methodName, *methodType.parameterArray())
            } catch (e: Throwable) {
            }
        }

        if (method == null) return null
        return ReflectedMethod(method)
    }

    fun createClassThroughCustomLoader(claz: Class<*>): MethodHandle {
        var loader = this::class.java.classLoader
        val urls: Array<URL> = (loader as URLClassLoader).urLs
        val reflectionLoader: Class<*> = object : URLClassLoader(urls, ClassLoader.getSystemClassLoader()) {
        }.loadClass(claz.name)
        var handle = MethodHandles.lookup().findConstructor(reflectionLoader, MethodType.methodType(Void.TYPE))
        return handle
    }

    class ReflectedField(private val field: Any) {
        fun get(): Any? = getFieldHandle.invoke(field)
        fun set(instance: Any?, value: Any?) {
            setFieldHandle.invoke(field, instance, value)
        }
    }

    class ReflectedMethod(private val method: Any) {
        fun invoke(instance: Any?, vararg arguments: Any?): Any? = invokeMethodHandle.invoke(method, instance, arguments)
    }
}