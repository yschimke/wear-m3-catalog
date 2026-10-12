#!/usr/bin/env python3
"""Writes the `Text` page's documents: `ListHeader`, `ListSubHeader`, `Text/Body`, `Text/Caption`.

  python3 uid-stickers/tools/text.py
"""
from uid import *

# ListHeader: a centred title in the header's content slot; the kit's `Alignment=Left` is a cell.
write(
    "list-header",
    document("list-header", "KitListHeader", "header", [
        node("header", "wear-m3/list-header", slots={"content": ["title"]}, modifiers=[width(180)]),
        node("title", "wear-m3/text", {"text": binding("label"), "textAlign": enum("center")}),
    ], {"label": string("Title")}),
    {
        "Left": [set_property("title", "textAlign", enum("start"))],
        "Right": [set_property("title", "textAlign", enum("end"))],
    },
    {"ListHeading": "ListHeading"},
)

# ListSubHeader: a start-aligned label filling the row; the kit's `Icon=Yes` adds a leading icon.
write(
    "list-sub-header",
    document("list-sub-header", "KitListSubHeader", "header", [
        node("header", "wear-m3/list-sub-header", slots={"label": ["label"]}, modifiers=[width(180)]),
        node("label", "wear-m3/text", {"text": binding("label"), "textAlign": enum("start")}, modifiers=[{"type": "fillMaxWidth"}]),
    ], {"label": string("Subtitle")}),
    {
        "Icon": [add_node("header", "icon", node("icon", "wear-m3/icon", {"iconKey": enum("add")}))],
        "Centred": [set_property("label", "textAlign", enum("center"))],
        "Icon centred": [
            add_node("header", "icon", node("icon", "wear-m3/icon", {"iconKey": enum("add")})),
            set_property("label", "textAlign", enum("center")),
        ],
    },
    {"ListSubHeading": "ListSubHeading"},
)

# Text/Body and Text/Caption: one `Text` at a type-scale role, centred, with `Alignment=Left`.
for key, name, role, hand in (
    ("text-body", "KitBodyText", "bodyMedium", "BodyText"),
    ("text-caption", "KitCaptionText", "labelSmall", "CaptionText"),
):
    write(
        key,
        document(key, name, "text", [
            node("text", "wear-m3/text", {"text": binding("text"), "style": enum(role), "textAlign": enum("center")}, modifiers=[width(160)]),
        ], {"text": string("Text")}),
        {
            "Left": [set_property("text", "textAlign", enum("start"))],
            "Right": [set_property("text", "textAlign", enum("end"))],
        },
        {hand: hand},
    )
