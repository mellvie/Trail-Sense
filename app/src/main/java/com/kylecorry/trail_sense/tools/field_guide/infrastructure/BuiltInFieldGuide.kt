package com.kylecorry.trail_sense.tools.field_guide.infrastructure

import android.content.Context
import com.kylecorry.trail_sense.R
import com.kylecorry.trail_sense.shared.text.TextUtils
import com.kylecorry.trail_sense.tools.field_guide.domain.FieldGuidePage
import com.kylecorry.trail_sense.tools.field_guide.domain.FieldGuidePageTag

object BuiltInFieldGuide {

    private data class BuiltInFieldGuidePage(
        val resourceId: Int,
        val imagePath: String,
        val tags: List<FieldGuidePageTag>
    )

    private val pages = listOf(
        BuiltInFieldGuidePage(
            R.raw.field_guide_squirrel,
            "field_guide/squirrel.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mammal,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Tundra,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_sunfish,
            "field_guide/sunfish.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Fish,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_black_bass,
            "field_guide/black_bass.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Fish,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_carp,
            "field_guide/carp.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Fish,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Crepuscular,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_crayfish,
            "field_guide/crayfish.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Crustacean,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_crab,
            "field_guide/crab.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Crustacean,
                FieldGuidePageTag.Marine,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Crepuscular,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_clam,
            "field_guide/clam.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mollusk,
                FieldGuidePageTag.Marine,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_mussel,
            "field_guide/mussel.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mollusk,
                FieldGuidePageTag.Marine,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_periwinkle,
            "field_guide/periwinkle.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mollusk,
                FieldGuidePageTag.Marine,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_rabbit,
            "field_guide/rabbit.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mammal,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Tundra,
                FieldGuidePageTag.Crepuscular,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_mouse,
            "field_guide/mouse.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Mammal,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Tundra,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Cave,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_grouse,
            "field_guide/grouse.webp",
            listOf(
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Bird,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Tundra,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_termite,
            "field_guide/termite.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Insect,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_grasshopper,
            "field_guide/grasshopper.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Insect,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_cricket,
            "field_guide/cricket.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Insect,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Cave,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_ant,
            "field_guide/ant.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Insect,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Desert,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Diurnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_earthworm,
            "field_guide/earthworm.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Worm,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_grub,
            "field_guide/grub.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Animal,
                FieldGuidePageTag.Insect,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Nocturnal,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_poison_ivy,
            "survival_guide/poison_ivy.webp",
            listOf(
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Dangerous,
                FieldGuidePageTag.Inedible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_stinging_nettle,
            "survival_guide/stinging_nettle.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Edible,
                FieldGuidePageTag.Dangerous
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_dandelion,
            "field_guide/dandelion.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Edible,
                FieldGuidePageTag.Medicinal
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_kelp,
            "field_guide/kelp.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Antarctica,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Marine,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Other,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_dock,
            "field_guide/dock.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_clover,
            "field_guide/clover.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_cattail,
            "field_guide/cattail.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Freshwater,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_bamboo,
            "field_guide/bamboo.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Mountain,
                FieldGuidePageTag.Wetland,
                FieldGuidePageTag.Edible,
                FieldGuidePageTag.Crafting
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_common_plantain,
            "field_guide/common_plantain.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Edible,
                FieldGuidePageTag.Medicinal
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_brambles,
            "field_guide/brambles.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Plant,
                FieldGuidePageTag.Grassland,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Urban,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_chicken_of_the_woods,
            "field_guide/chicken_of_the_woods.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Fungus,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_bolete,
            "field_guide/bolete.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Fungus,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_morel,
            "field_guide/morel.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Fungus,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_oyster_mushroom,
            "field_guide/oyster_mushroom.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Fungus,
                FieldGuidePageTag.Forest,
                FieldGuidePageTag.Edible
            )
        ),
        BuiltInFieldGuidePage(
            R.raw.field_guide_chert,
            "field_guide/chert.webp",
            listOf(
                FieldGuidePageTag.Africa,
                FieldGuidePageTag.Asia,
                FieldGuidePageTag.Australia,
                FieldGuidePageTag.Europe,
                FieldGuidePageTag.NorthAmerica,
                FieldGuidePageTag.SouthAmerica,
                FieldGuidePageTag.Rock,
                FieldGuidePageTag.Crafting
            )
        )
    )

    fun getFieldGuidePage(context: Context, id: Long): FieldGuidePage? {
        return pages.getOrNull(-id.toInt())?.let { loadPage(context, it, -id.toInt()) }
    }

    fun getFieldGuide(context: Context): List<FieldGuidePage> {
        return pages.mapIndexed { index, page ->
            loadPage(context, page, index)
        }
    }

    private fun loadPage(
        context: Context,
        page: BuiltInFieldGuidePage,
        index: Int
    ): FieldGuidePage {
        val text = TextUtils.loadTextFromResources(context, page.resourceId)
        val lines = text.split("\n")
        val name = lines.first()
        val notes = lines.drop(1).joinToString("\n").trim()
        return FieldGuidePage(
            -index.toLong(),
            name,
            listOf("android-assets://${page.imagePath}"),
            page.tags,
            notes,
            isReadOnly = true
        )
    }
}