#!/usr/bin/env python3
"""Writes the cards' documents: `Card/Outlined`.

  python3 uid-stickers/tools/cards.py
"""
from uid import *

# Card/Outlined: the title-less outlined container of the kit's width, its copy in the one slot.
write(
    "outlined-card",
    document("outlined-card", "KitOutlinedCard", "card", [
        node("card", "wear-m3/outlined-card", slots={"content": ["content"]}, modifiers=[width(172)], events=["click"]),
        node("content", "wear-m3/text", {"text": binding("content")}),
    ], {"content": string("Content")}),
    {},
    {"OutlineCard": "OutlineCard"},
)
