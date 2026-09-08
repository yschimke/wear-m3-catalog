# `remote-m3`'s front-door hero

The one sticker the preview server features for this catalog, before and after moving
`display.hero` from `Scaffold` to `AppCard`.

| File | What it shows |
| --- | --- |
| `hero-before-scaffold.png` | `Scaffold` — the baked hero the live front door served, four bare list rows under a clock frozen at 10:10. |
| `hero-after-appcard.png` | `AppCard` at `__ideal__default__compact` — container, icon slot, app name, trailing time and a title/content type ramp. |

Both are the deployment's own renders, fetched from `preview.coo.ee` (`/hero/remote-m3/…` and
`/remote-m3/render/appcard__ideal__default__compact.png`) rather than re-rendered here, so they are
the pixels the front door actually served.

The copy in both is placeholder — that is not the difference. The difference is how much
`RemoteAppCard` geometry a card-sized sticker gets to show.
