"""
Generate a complete Material 3 colour scheme from an image, the way Android does.

Produces the values in app/src/main/java/com/noty/app/ui/theme/Color.kt. The palette in the
app today came from:

    python scripts/generate_color_scheme.py --seed 0xFF4CAF50

#4CAF50 is Material Green 500 — a standard, citable value rather than one tuned by eye. It
was cross-checked against a reference rendering of the app and lands within 2/255 of it.

TonalSpot is the default variant on purpose: it is what Android generates for dynamic colour,
so the Settings toggle changes shade rather than character. A more saturated variant would
make the two sides of that switch feel like different palettes.

Passing an image instead extracts a seed from it, which is what the platform does for dynamic
colour. Note the two do not have to agree: the platform crops a wallpaper to the screen before
quantising, so seeding from the full file samples different pixels and can land a few units
away from what the device shows.

If you re-seed, also update the surface colours in res/values/colors.xml and
res/values-night/themes.xml. Those have to match the scheme or the window flashes the wrong
colour before Compose draws.

Requires: pip install materialyoucolor

Same pipeline as the platform's dynamic colour: Celebi quantisation over the pixels,
Score to pick the seed, then a DynamicScheme to derive every role. Emits Compose
lightColorScheme()/darkColorScheme() blocks covering the roles Noty's UI reads.

    python scripts/generate_color_scheme.py <image> [--variant tonal_spot] [--spec 2021]
"""
import argparse
import sys

from PIL import Image
from materialyoucolor.quantize import QuantizeCelebi
from materialyoucolor.score.score import Score
from materialyoucolor.hct import Hct
from materialyoucolor.dynamiccolor.material_dynamic_colors import MaterialDynamicColors
from materialyoucolor.scheme.scheme_tonal_spot import SchemeTonalSpot
from materialyoucolor.scheme.scheme_vibrant import SchemeVibrant
from materialyoucolor.scheme.scheme_expressive import SchemeExpressive
from materialyoucolor.scheme.scheme_content import SchemeContent
from materialyoucolor.scheme.scheme_fidelity import SchemeFidelity
from materialyoucolor.scheme.scheme_neutral import SchemeNeutral

VARIANTS = {
    "tonal_spot": SchemeTonalSpot,   # what Android uses by default
    "vibrant": SchemeVibrant,
    "expressive": SchemeExpressive,
    "content": SchemeContent,
    "fidelity": SchemeFidelity,
    "neutral": SchemeNeutral,
}

# Every role in Compose's ColorScheme, in the order Color.kt lists them.
ROLES = [
    "primary", "onPrimary", "primaryContainer", "onPrimaryContainer", "inversePrimary",
    "secondary", "onSecondary", "secondaryContainer", "onSecondaryContainer",
    "tertiary", "onTertiary", "tertiaryContainer", "onTertiaryContainer",
    "background", "onBackground", "surface", "onSurface",
    "surfaceVariant", "onSurfaceVariant", "surfaceTint",
    "inverseSurface", "inverseOnSurface",
    "error", "onError", "errorContainer", "onErrorContainer",
    "outline", "outlineVariant", "scrim",
    "surfaceBright", "surfaceDim", "surfaceContainer", "surfaceContainerHigh",
    "surfaceContainerHighest", "surfaceContainerLow", "surfaceContainerLowest",
]


def seed_from_image(path):
    image = Image.open(path).convert("RGB")
    # Cap the work: quantisation is O(pixels) and a wallpaper is far more detail than
    # the seed needs. Android samples a scaled bitmap for the same reason.
    image.thumbnail((256, 256))
    pixels = [(r, g, b, 255) for r, g, b in list(image.getdata())]
    quantized = QuantizeCelebi(pixels, 128)
    ranked = Score.score(quantized)
    return ranked[0], ranked[:5]


def argb_to_hex(argb):
    return "0x%08X" % (argb & 0xFFFFFFFF)


def roles(scheme, names):
    out = {}
    for name in names:
        prop = getattr(MaterialDynamicColors, name, None)
        if prop is None:
            continue
        out[name] = scheme.get_hct(prop).to_int()
    return out


def emit(suffix, values):
    """Color.kt's naming: primaryLight / primaryDark, per Material Theme Builder."""
    return "\n".join(
        "val %s%s = Color(%s)" % (role, suffix, argb_to_hex(argb))
        for role, argb in values.items()
    )


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("image", nargs="?", help="wallpaper to derive a seed from; omit with --seed")
    ap.add_argument("--variant", default="tonal_spot", choices=sorted(VARIANTS))
    # 2021, not the library's 2025 default: the platform's dynamic colour — and therefore
    # Compose's dynamicLightColorScheme() — is the 2021 spec, and a default scheme built to
    # the 2025 one would not match the app's own dynamic mode on the same wallpaper.
    ap.add_argument("--spec", default="2021", choices=["2021", "2025"])
    ap.add_argument("--seed", help="override the extracted seed, e.g. 0xFF4F5CD3")
    args = ap.parse_args()

    if args.seed:
        seed = int(args.seed, 16)
        top = [seed]
    elif args.image:
        seed, top = seed_from_image(args.image)
    else:
        ap.error("give an image, a --seed, or both")

    print("# source     : %s" % (args.image or "--seed"))
    print("# variant    : %s (spec %s)" % (args.variant, args.spec))
    print("# seed       : #%06X" % (seed & 0xFFFFFF))
    print("# candidates : %s" % ", ".join("#%06X" % (c & 0xFFFFFF) for c in top))
    print()

    cls = VARIANTS[args.variant]
    for dark in (False, True):
        scheme = cls(Hct.from_int(seed), dark, 0.0, spec_version=args.spec)
        print("// %s scheme" % ("Dark" if dark else "Light"))
        print(emit("Dark" if dark else "Light", roles(scheme, ROLES)))
        print()


if __name__ == "__main__":
    sys.exit(main())
