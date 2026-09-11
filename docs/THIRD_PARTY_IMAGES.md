# Third-party images

Everything this catalog draws is generated in code — see
[`CatalogImage.kt`](../catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogImage.kt) for why —
with one deliberate exception, recorded here.

## Account avatars

| File | Source | Original |
| --- | --- | --- |
| [`catalog/src/main/res/drawable-nodpi/catalog_avatar_1.jpg`](../catalog/src/main/res/drawable-nodpi/catalog_avatar_1.jpg) | [cjdowner/interfaces](https://github.com/cjdowner/interfaces) `256 x 256/009.jpg` | [pexels.com/photo/38554](https://www.pexels.com/photo/woman-in-black-scoop-neck-shirt-smiling-38554/) |
| [`catalog/src/main/res/drawable-nodpi/catalog_avatar_2.jpg`](../catalog/src/main/res/drawable-nodpi/catalog_avatar_2.jpg) | [cjdowner/interfaces](https://github.com/cjdowner/interfaces) `256 x 256/014.jpg` | [pexels.com/photo/50855](https://www.pexels.com/photo/person-man-confident-business-50855/) |

**Licence: [CC0 1.0 Universal](https://creativecommons.org/publicdomain/zero/1.0/)** (public domain
dedication). The pack states it plainly: free for personal and commercial use, modifiable, copyable,
redistributable, and **attribution is not required**. It is given anyway — a contributor who finds
photographs of strangers in a design repository should be able to see where they came from without
having to ask.

The one condition CC0 does not lift is the one the pack names: identifiable people must not be shown
in a bad light, or in a way they would find offensive. These are seed data for a sign-in screen —
`Maya` and `Sam` at `example.com` — which is as neutral a use as the pack has.

Both were copied unmodified from the pack's `256 x 256` set (~45 KB each). They are cropped to a
circle at draw time rather than in the file, by
[`CatalogAvatar`](../catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogAvatar.kt) — see its
KDoc, and [#435](https://github.com/yschimke/wear-m3-catalog/issues/435), for why the mask belongs at
the call site.

## Why this is the only exception

[`CatalogImage`](../catalog/src/main/kotlin/ee/schimke/wearm3catalog/CatalogImage.kt)'s KDoc argues
for drawing rather than shipping: a committed photograph puts a licence question in front of every
contributor, and a drawn fill weighs nothing and renders identically on every publish, which a
catalog whose delivery branch is diffed nightly needs.

That argument holds wherever the picture is a *placeholder* — an empty kit cell, album art, an app
glyph — because a gradient loses nothing there. It does not hold for the one slot whose subject is a
human face. At `ButtonDefaults.LargeIconSize` the question a reader is asking is whether a
**photograph** survives the slot, and only a photograph answers it. A CC0 pack settles the licence
question in this file rather than leaving it open, which is the cost the original argument was
avoiding.

Adding another image asset means adding a row here, with its licence.
