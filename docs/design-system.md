# AURA Design System

## Identity
Obsidian is the default theme: near-black layered charcoal surfaces,
hairline borders, silver-white text, and a single deep warm red accent.

Red usage is restricted to:
- active navigation
- progress indicators
- selected or active controls
- the current track
- small signature brand details

Red is never a surface and never a gradient wash.

## Tokens
- Colors: `AuraColors` (background, surface, surfaceElevated, borderSubtle,
  textPrimary, textSecondary, textMuted, auraRed, auraRedSoft, error).
- Spacing: `AuraSpacing` (4, 8, 12, 16, 20, 24, 32, 40, 48 dp).
  Arbitrary dp values are not allowed in screens.
- Shape: `AuraShape` (small 8, medium 12, large 20, pill, artwork).
- Typography: `AuraTypography` (display, headline, title, body, metadata, label).
  Display and headline use the system serif for an editorial musical voice.
  A bundled open-licensed serif may replace it later without call-site changes.
- Motion: `AuraMotion` (Fast 150, Standard 300, Slow 500, Artwork 400,
  Atmosphere 600 ms; Standard and Decelerate easings; LocalAuraReduceMotion).

## Components
- AuraArtwork: cached artwork surface with deterministic serif-A fallback.
- AuraLoadingState, AuraEmptyState, AuraErrorState: mandatory screen states.
  Empty states must teach the next action. Errors must offer recovery.

## Reduced motion
`AuraMotion.LocalAuraReduceMotion` gates animation specs.
The Reduce Motion setting (Settings, Appearance) will provide it.
Until then it defaults to false.

## Atmosphere (planned)
The atmosphere engine will output semantic AtmosphereTokens from cached
palette analysis of the current artwork, with contrast validation and
deterministic fallbacks. It replaces `AuraColors.dynamicAccent`.

## Accessibility
- Hierarchy is communicated by type size, weight, and spacing, not color alone.
- All interactive targets meet minimum touch size.
- Loading indicators expose contentDescription.
- Contrast is validated for every text and control combination.