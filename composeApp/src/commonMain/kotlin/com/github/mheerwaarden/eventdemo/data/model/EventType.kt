package com.github.mheerwaarden.eventdemo.data.model

import androidx.compose.ui.graphics.Color
import com.github.mheerwaarden.eventdemo.resources.*
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import org.jetbrains.compose.resources.StringResource

enum class EventType(val text: StringResource, val color: Color) {
    ACTIVITY(Res.string.event_type_activity, HtmlColors.AQUAMARINE.color),
    BOAT_TRIP(Res.string.event_type_boat_trip, HtmlColors.BROWN.color),
    COMPANY_PARTY(Res.string.event_type_company_party, HtmlColors.BLUE.color),
    CONFERENCE(Res.string.event_type_conference, HtmlColors.BLUE_VIOLET.color),
    CORPORATE_HACKATHON(Res.string.event_type_corporate_hackathon, HtmlColors.CORAL.color),
    CORPORATE_OFF_SITE(Res.string.event_type_corporate_off_site, HtmlColors.CORNFLOWER_BLUE.color),
    CULTURAL_EXPERIENCE(Res.string.event_type_cultural_experience, HtmlColors.CRIMSON.color),
    EXECUTIVE_MEETING(Res.string.event_type_executive_meeting, HtmlColors.DARK_CYAN.color),
    EXHIBITION(Res.string.event_type_exhibition, HtmlColors.DARK_GOLDEN_ROD.color),
    EXPERIMENTAL_MARKETING_ACTIVATION(Res.string.event_type_experimental_marketing_activation, HtmlColors.DARK_GREEN.color),
    FESTIVAL(Res.string.event_type_festival, HtmlColors.DARK_OLIVE_GREEN.color),
    FOOD_EXPERIENCE(Res.string.event_type_food_experience, HtmlColors.DARK_ORANGE.color),
    FOOD_TRUCK_FESTIVAL(Res.string.event_type_food_truck_festival, HtmlColors.DARK_RED.color),
    GAME(Res.string.event_type_game, HtmlColors.DARK_SEA_GREEN.color),
    INCENTIVE(Res.string.event_type_incentive, HtmlColors.DARK_SLATE_BLUE.color),
    MEETING(Res.string.event_type_meeting, HtmlColors.FOREST_GREEN.color),
    NETWORKING(Res.string.event_type_networking, HtmlColors.GOLD.color),
    ONLINE_EVENT(Res.string.event_type_online_event, HtmlColors.GOLDEN_ROD.color),
    OUTDOOR_EXPERIENCE(Res.string.event_type_outdoor_experience, HtmlColors.INDIGO.color),
    PRODUCT_LAUNCH(Res.string.event_type_product_launch, HtmlColors.LIGHT_GRAY.color),
    SEMINAR(Res.string.event_type_seminar, HtmlColors.LIGHT_PINK.color),
    SOCIAL_EVENT(Res.string.event_type_social_event, HtmlColors.LIGHT_SALMON.color),
    SPORT(Res.string.event_type_sport, HtmlColors.LIGHT_SKY_BLUE.color),
    TEAM_BUILDING_ACTIVITY(Res.string.event_type_team_building_activity, HtmlColors.LIGHT_SLATE_GRAY.color),
    TRADE_SHOW(Res.string.event_type_trade_show, HtmlColors.MEDIUM_PURPLE.color),
    VIRTUAL_RECRUITING_EVENT(Res.string.event_type_virtual_recruiting_event, HtmlColors.MEDIUM_TURQUOISE.color),
    VIRTUAL_TRAINING_SESSION(Res.string.event_type_virtual_training_session, HtmlColors.OLIVE_DRAB.color),
    WINE_EXPERIENCE(Res.string.event_type_wine_experience, HtmlColors.ROYAL_BLUE.color),
    WORKSHOPS(Res.string.event_type_workshops, HtmlColors.SIENNA.color),
}

