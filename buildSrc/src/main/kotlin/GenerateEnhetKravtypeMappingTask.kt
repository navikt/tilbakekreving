import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.joinToCode
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import kotlin.streams.asSequence

private const val DOMAIN_PKG = "no.nav.tilbakekreving.domain"
private const val AUTH_MODEL_PKG = "no.nav.tilbakekreving.infrastructure.auth.model"

private val CSV_FORMAT: CSVFormat =
    CSVFormat.Builder
        .create(CSVFormat.RFC4180)
        .setDelimiter(';')
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreSurroundingSpaces(true)
        .setTrim(true)
        .get()

abstract class GenerateEnhetKravtypeMappingTask : DefaultTask() {
    @get:InputFile
    abstract val csvFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val mapping = parseCsvToMapping()
        val fileSpec = buildFileSpec(mapping)
        fileSpec.writeTo(outputDir.get().asFile)

        println("Generated enhetKravtypeMapping with ${mapping.size} enheter")
    }

    private fun parseCsvToMapping(): Map<String, Set<String>> {
        val text =
            csvFile
                .get()
                .asFile
                .readText(Charsets.UTF_8)
                .removePrefix("\uFEFF")
        return CSVParser.parse(text, CSV_FORMAT).use { parser ->
            parser
                .stream()
                .asSequence()
                .filter { record ->
                    val kode = record["Ekstern kravkode"]
                    !kode.isNullOrBlank() && kode != "0" && kode != "#N/A"
                }.groupBy(
                    keySelector = { it["Enhetsnummer"] },
                    valueTransform = { it["Ekstern kravkode"] },
                ).mapValues { (_, codes) -> codes.toSortedSet() }
                .toSortedMap()
        }
    }

    private fun buildFileSpec(mapping: Map<String, Set<String>>): FileSpec {
        val enhetsnummerCn = ClassName(AUTH_MODEL_PKG, "Enhetsnummer")
        val kravtypeCn = ClassName(DOMAIN_PKG, "Kravtype")
        val mapType =
            Map::class.asClassName().parameterizedBy(
                enhetsnummerCn,
                Set::class.asClassName().parameterizedBy(kravtypeCn),
            )

        val mapEntries =
            mapping.entries
                .map { (enhet, kravkodes) ->
                    val setBlock =
                        kravkodes
                            .map { CodeBlock.of("%T.%L", kravtypeCn, it) }
                            .joinToCode(prefix = "setOf(", suffix = ")")
                    CodeBlock.of("%T(%S) to %L", enhetsnummerCn, enhet, setBlock)
                }.joinToCode(prefix = "mapOf(\n⇥", suffix = ",\n⇤)", separator = ",\n")

        val property = PropertySpec.builder("enhetKravtypeMapping", mapType).initializer(mapEntries).build()

        return FileSpec
            .builder(AUTH_MODEL_PKG, "EnhetKravtypeMapping")
            .addFileComment("Generated from Tilganger-kravtyper(EnheterKravtyper).csv — do not edit manually.")
            .addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .addMember("%S", "RedundantVisibilityModifier")
                    .addMember("%S", "unused")
                    .build(),
            ).addProperty(property)
            .build()
    }
}
