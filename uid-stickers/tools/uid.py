"""Shared helpers for the scripts that write `stickers/*.uid` documents."""
import json, os

STICKERS = os.path.join(os.path.dirname(os.path.abspath(__file__)), "../stickers")


def document(key, name, root, nodes, arguments):
    """A sticker body: one component definition (`root` and its descendants), placed once."""
    return {
        "schema": "compose-ui-builder-document/v1-candidate",
        "id": key,
        "title": name,
        "revision": 0,
        "catalogPin": {"systemId": "wear-m3", "catalogRevision": "candidate", "capabilityDigest": "candidate", "nativeRuntimeId": "candidate"},
        "environment": {"widthDp": 192, "heightDp": 192, "density": 2.0, "theme": "dark", "locale": "en-US", "fontScale": 1, "layoutDirection": "ltr"},
        "roots": ["place"],
        "components": {key: {"name": name, "root": root}},
        "nodes": {
            "place": {"id": "place", "componentId": "design/component-instance", "component": {"componentKey": key, "arguments": arguments}},
            **{n["id"]: n for n in nodes},
        },
    }


def node(id_, component, properties=None, slots=None, modifiers=None, events=None):
    n = {"id": id_, "componentId": component, "properties": properties or {}, "slots": slots or {}}
    if modifiers:
        n["modifiers"] = modifiers
    if events:
        n["eventBindings"] = {e: [] for e in events}
    return n


def binding(name):
    return {"type": "binding", "value": name}


def enum(value):
    return {"type": "enum", "value": value}


def string(value):
    return {"type": "string", "value": value}


def width(dp):
    return {"type": "width", "widthDp": dp}


def size(dp):
    return {"type": "size", "widthDp": dp, "heightDp": dp}


def set_property(node_id, prop, value):
    return {"type": "setProperty", "nodeId": node_id, "property": prop, "value": value}


def add_node(parent, slot, child):
    return {"type": "addNode", "parentId": parent, "slot": slot, "node": child}


def write(key, doc, variants, pairs):
    for path, value in ((f"{key}.uid", doc), (f"{key}.json", {"variants": variants, "pairs": pairs})):
        with open(os.path.join(STICKERS, path), "w") as out:
            json.dump(value, out, indent=2)
            out.write("\n")
    print(f"stickers/{key}.uid")
