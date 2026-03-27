import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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

private val CSV_FORMAT: CSVFormat =
    CSVFormat.Builder
        .create(CSVFormat.RFC4180)
        .setDelimiter(';')
        .setHeader()
        .setSkipHeaderRecord(true)
        .setIgnoreSurroundingSpaces(true)
        .setTrim(true)
        .get()

abstract class GenerateKravtypeTask : DefaultTask() {
    @get:InputFile
    abstract val csvFile: RegularFileProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val grouped = parseCsvToKravtyper()
        val fileSpec = buildFileSpec(grouped)
        fileSpec.writeTo(outputDir.get().asFile)

        println("Generated DefinertKravtype enum with ${grouped.size} entries")
    }

    private fun parseCsvToKravtyper(): Map<String, KravtypeEntry> {
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
                    val kode = record["Ekstern kravkode (Nav)"]
                    !kode.isNullOrBlank() && kode != "0"
                }.groupingBy { it["Ekstern kravkode (Nav)"] }
                .fold({ key, record ->
                    KravtypeEntry(key, mutableListOf(), record["Kravtype"].cleanField())
                }) { _, entry, record ->
                    entry.also {
                        it.stønadsinfo.add(record["Stønadstype"] to record["Stønadsnavn"])
                    }
                }
        }
    }

    private fun buildFileSpec(grouped: Map<String, KravtypeEntry>): FileSpec {
        val stønadsinfoCn = ClassName(DOMAIN_PKG, "Stønadsinfo")

        val stønadsinfoType =
            TypeSpec
                .classBuilder("Stønadsinfo")
                .addModifiers(KModifier.DATA)
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("stønadstype", String::class)
                        .addParameter("stønadsnavn", String::class)
                        .build(),
                ).addProperty(PropertySpec.builder("stønadstype", String::class).initializer("stønadstype").build())
                .addProperty(PropertySpec.builder("stønadsnavn", String::class).initializer("stønadsnavn").build())
                .build()

        val enumBuilder =
            TypeSpec
                .enumBuilder("DefinertKravtype")
                .primaryConstructor(
                    FunSpec
                        .constructorBuilder()
                        .addParameter("stønadsinfo", List::class.asClassName().parameterizedBy(stønadsinfoCn))
                        .addParameter("kravtypeBeskrivelse", String::class)
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("stønadsinfo", List::class.asClassName().parameterizedBy(stønadsinfoCn))
                        .initializer("stønadsinfo")
                        .build(),
                ).addProperty(
                    PropertySpec
                        .builder("kravtypeBeskrivelse", String::class)
                        .initializer("kravtypeBeskrivelse")
                        .build(),
                )

        grouped.values.forEach { entry ->
            val infoList =
                entry.stønadsinfo
                    .map { (type, navn) -> CodeBlock.of("%T(%S, %S)", stønadsinfoCn, type, navn) }
                    .joinToCode(prefix = "listOf(", suffix = ")")

            enumBuilder.addEnumConstant(
                entry.eksternKravkode,
                TypeSpec
                    .anonymousClassBuilder()
                    .addSuperclassConstructorParameter(infoList)
                    .addSuperclassConstructorParameter("%S", entry.kravtypeBeskrivelse)
                    .build(),
            )
        }

        return FileSpec
            .builder(DOMAIN_PKG, "DefinertKravtype")
            .addFileComment("Generated from Tilganger-kravtyper(Kravtyper).csv — do not edit manually.")
            .addAnnotation(
                AnnotationSpec
                    .builder(Suppress::class)
                    .addMember("%S", "RedundantVisibilityModifier")
                    .addMember("%S", "unused")
                    .build(),
            ).addType(stønadsinfoType)
            .addType(enumBuilder.build())
            .build()
    }

    private data class KravtypeEntry(
        val eksternKravkode: String,
        val stønadsinfo: MutableList<Pair<String, String>>,
        val kravtypeBeskrivelse: String,
    )

    private fun String.cleanField() = replace("\n", "").replace("\r", "").trim()
}
