#!/usr/bin/env python3
"""Writes the label-button documents (`Button/Tonal`, `Button/Outlined`, `Button/Child`).

The three share one body and one set of cells (`@ButtonLayoutCells`): a label, and the kit's
`Icon=Yes` cells, which add a leading `Add` icon at the kit's three sizes, the secondary label the
kit couples to it, and a start-aligned label. Only the component and its width differ.

  python3 uid-stickers/tools/buttons.py
"""
import json, os

HERE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../stickers")

# id → (component name, wear-m3 component, hand-written function, width in dp or None)
BUTTONS = {
    "tonal-button": ("KitTonalButton", "wear-m3/filled-tonal-button", "TonalButton", 172),
    "outlined-button": ("KitOutlinedButton", "wear-m3/outlined-button", "OutlineButton", 172),
    "child-button": ("KitChildButton", "wear-m3/child-button", "ChildLabelButton", None),
}

# `ButtonDefaults.IconSize`, `LargeIconSize`, `ExtraLargeIconSize`.
ICON_SIZES = {"Icon": 26, "Icon large": 32, "Icon extra large": 36}


def document(key, name, component, width):
    button = {
        "id": "button",
        "componentId": component,
        "properties": {"enabled": {"type": "binding", "value": "enabled"}},
        "slots": {"label": ["label"]},
        "eventBindings": {"click": []},
    }
    if width:
        button["modifiers"] = [{"type": "width", "widthDp": width}]
    return {
        "schema": "compose-ui-builder-document/v1-candidate",
        "id": key,
        "title": name,
        "revision": 0,
        "catalogPin": {"systemId": "wear-m3", "catalogRevision": "candidate", "capabilityDigest": "candidate", "nativeRuntimeId": "candidate"},
        "environment": {"widthDp": 192, "heightDp": 192, "density": 2.0, "theme": "dark", "locale": "en-US", "fontScale": 1, "layoutDirection": "ltr"},
        "roots": ["place"],
        "components": {key: {"name": name, "root": "button"}},
        "nodes": {
            "place": {
                "id": "place",
                "componentId": "design/component-instance",
                "component": {
                    "componentKey": key,
                    "arguments": {
                        "label": {"type": "string", "value": "Label"},
                        "secondaryLabel": {"type": "string", "value": "Secondary label"},
                        "enabled": {"type": "bool", "value": True},
                    },
                },
            },
            "label": {"id": "label", "componentId": "wear-m3/text", "properties": {"text": {"type": "binding", "value": "label"}}, "slots": {}},
            "button": button,
        },
    }


def cells():
    left = [
        {"type": "setModifiers", "nodeId": "label", "modifiers": [{"type": "fillMaxWidth"}]},
        {"type": "setProperty", "nodeId": "label", "property": "textAlign", "value": {"type": "enum", "value": "start"}},
    ]

    def icon(dp):
        return [
            {"type": "addNode", "parentId": "button", "slot": "icon", "node": {
                "id": "icon", "componentId": "wear-m3/icon", "properties": {"iconKey": {"type": "enum", "value": "add"}},
                "slots": {}, "modifiers": [{"type": "size", "widthDp": dp, "heightDp": dp}]}},
            {"type": "addNode", "parentId": "button", "slot": "secondaryLabel", "node": {
                "id": "secondary", "componentId": "wear-m3/text",
                "properties": {"text": {"type": "binding", "value": "secondaryLabel"}}, "slots": {}}},
        ]

    return {"Left": left, **{cell: icon(dp) + left for cell, dp in ICON_SIZES.items()}}


def write(path, value):
    with open(path, "w") as out:
        json.dump(value, out, indent=2)
        out.write("\n")


for key, (name, component, hand, width) in BUTTONS.items():
    write(os.path.join(HERE, f"{key}.uid"), document(key, name, component, width))
    write(os.path.join(HERE, f"{key}.json"), {"variants": cells(), "pairs": {hand: hand}})
    print(f"stickers/{key}.uid")
