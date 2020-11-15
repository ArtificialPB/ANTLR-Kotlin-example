package com.example

import com.example.query.IndicatorQueryBaseListener
import com.example.query.IndicatorQueryLexer
import com.example.query.IndicatorQueryParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTreeWalker

private val DEFAULT_INDICATOR_PACKAGES = listOf(
        "com.example.indicators",
)

data class IndicatorMeta(val clazz: Class<*>, val params: Array<Any>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IndicatorMeta

        if (clazz != other.clazz) return false
        if (!params.contentEquals(other.params)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = clazz.hashCode()
        result = 31 * result + params.contentHashCode()
        return result
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        @Throws(Exception::class)
        fun fromQuery(query: String, additionalPackages: List<String> = emptyList()): IndicatorMeta {
            val lexer = IndicatorQueryLexer(CharStreams.fromString(query))
            val tokens = CommonTokenStream(lexer)
            val parser = IndicatorQueryParser(tokens)

            return IndicatorQueryDecoder(parser).decode().build(additionalPackages)
        }

        @Throws(ClassNotFoundException::class)
        private fun getIndicatorClass(name: String, additionalPackages: List<String>): Class<*> {
            for (pack in DEFAULT_INDICATOR_PACKAGES) {
                try {
                    return Class.forName("$pack.$name")
                } catch (ignored: ClassNotFoundException) {
                }
            }
            for (pack in additionalPackages) {
                try {
                    return Class.forName("$pack.$name")
                } catch (ignored: ClassNotFoundException) {
                }
            }
            val allPackages = ArrayList(DEFAULT_INDICATOR_PACKAGES) + additionalPackages
            throw ClassNotFoundException("Indicator \"$name\" not found in any defined packaged: $allPackages")
        }
    }

    class Builder {
        var name: String? = null
        val params = ArrayList<Any>()

        @Throws(ClassNotFoundException::class)
        fun build(additionalPackages: List<String>): IndicatorMeta {
            val builtParams = ArrayList<Any>()
            params.forEach {
                var value = it
                if (it is Builder) {
                    value = it.build(additionalPackages)
                }
                builtParams.add(value)
            }
            return IndicatorMeta(getIndicatorClass(name!!, additionalPackages), builtParams.toArray())
        }
    }
}

private class IndicatorQueryDecoder(private val parser: IndicatorQueryParser) : IndicatorQueryBaseListener() {
    private val builders = ArrayDeque<IndicatorMeta.Builder>()

    override fun enterIndicator(ctx: IndicatorQueryParser.IndicatorContext) {
        builders.addFirst(IndicatorMeta.Builder())
    }

    override fun exitIndicator(ctx: IndicatorQueryParser.IndicatorContext) {
        val finished = builders.removeFirst()

        val parent = builders.firstOrNull()
        if (parent == null) {
            builders.addFirst(finished)
        } else {
            parent.params.add(finished)
        }
    }

    override fun enterIndicatorName(ctx: IndicatorQueryParser.IndicatorNameContext) {
        builders.first().name = ctx.WORD().text
    }

    override fun enterParameter(ctx: IndicatorQueryParser.ParameterContext) {
        if (ctx.indicator() != null) {
            return
        }
        if (ctx.BOOLEAN() != null) {
            builders.first().params.add(ctx.BOOLEAN().text)
            return
        }
        if (ctx.WORD() != null) {
            builders.first().params.add(ctx.WORD().text)
            return
        }
        if (ctx.NUMBER() != null) {
            builders.first().params.add(ctx.NUMBER().text)
            return
        }
    }

    fun decode(): IndicatorMeta.Builder {
        ParseTreeWalker().walk(this, parser.indicator())
        return builders.first()
    }
}