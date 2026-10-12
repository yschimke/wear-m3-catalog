#!/usr/bin/env python3
"""Writes the selection controls' documents: `CheckboxButton`, `SwitchButton`, `RadioButton`.

Each is a labelled row of the kit's width with a secondary label; `checked` (`selected`) and
`enabled` are bindings, and the change is an intent the shell wires to its `toggleable` state. The
kit's `Split (2 tap targets)=Yes` cells are the `Split…` sibling function: a second callback for
the container, and the toggle's own content description.

  python3 uid-stickers/tools/selection.py
"""
from uid import *

# key → (component name, control, split control, hand-written, state property, change event, split change event, split description)
CONTROLS = {
    "checkbox-button": ("KitCheckboxButton", "wear-m3/checkbox-button", "wear-m3/split-checkbox-button", "CheckboxRow",
                        "checked", "checkedChange", "checkedChange", "toggleContentDescription"),
    "switch-button": ("KitSwitchButton", "wear-m3/switch-button", "wear-m3/split-switch-button", "SwitchRow",
                      "checked", "checkedChange", "checkedChange", "toggleContentDescription"),
    "radio-button": ("KitRadioButton", "wear-m3/radio-button", "wear-m3/split-radio-button", "RadioRow",
                     "selected", "select", "selectionClick", "selectionContentDescription"),
}

for key, (name, control, split, hand, state, change, split_change, description) in CONTROLS.items():
    doc = document(key, name, "control", [
        node("control", control, {state: binding("checked"), "enabled": binding("enabled")},
             slots={"label": ["label"], "secondaryLabel": ["secondary"]}, modifiers=[width(172)], events=[change]),
        node("label", "wear-m3/text", {"text": binding("label")}),
        node("secondary", "wear-m3/text", {"text": binding("secondaryLabel")}),
    ], {
        "checked": {"type": "bool", "value": True},
        "enabled": {"type": "bool", "value": True},
        "label": string("Label"),
        "secondaryLabel": string("Secondary label"),
    })
    variants = {"Split": [
        {"type": "setComponentId", "nodeId": "control", "componentId": split},
        set_property("control", description, binding("label")),
        {"type": "setEventBindings", "nodeId": "control", "eventBindings": {split_change: [], "containerClick": []}},
    ]}
    write(key, doc, variants, {hand: hand})
