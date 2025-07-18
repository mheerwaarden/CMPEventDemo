package com.github.mheerwaarden.eventdemo.data.model

import com.github.mheerwaarden.eventdemo.resources.Res
import com.github.mheerwaarden.eventdemo.resources.event_type_activity
import com.github.mheerwaarden.eventdemo.resources.event_type_boat_trip
import com.github.mheerwaarden.eventdemo.resources.event_type_company_party
import com.github.mheerwaarden.eventdemo.resources.event_type_conference
import com.github.mheerwaarden.eventdemo.resources.event_type_corporate_hackathon
import com.github.mheerwaarden.eventdemo.resources.event_type_corporate_off_site
import com.github.mheerwaarden.eventdemo.resources.event_type_cultural_experience
import com.github.mheerwaarden.eventdemo.resources.event_type_executive_meeting
import com.github.mheerwaarden.eventdemo.resources.event_type_exhibition
import com.github.mheerwaarden.eventdemo.resources.event_type_experimental_marketing_activation
import com.github.mheerwaarden.eventdemo.resources.event_type_festival
import com.github.mheerwaarden.eventdemo.resources.event_type_food_experience
import com.github.mheerwaarden.eventdemo.resources.event_type_food_truck_festival
import com.github.mheerwaarden.eventdemo.resources.event_type_game
import com.github.mheerwaarden.eventdemo.resources.event_type_incentive
import com.github.mheerwaarden.eventdemo.resources.event_type_meeting
import com.github.mheerwaarden.eventdemo.resources.event_type_networking
import com.github.mheerwaarden.eventdemo.resources.event_type_online_event
import com.github.mheerwaarden.eventdemo.resources.event_type_outdoor_experience
import com.github.mheerwaarden.eventdemo.resources.event_type_product_launch
import com.github.mheerwaarden.eventdemo.resources.event_type_seminar
import com.github.mheerwaarden.eventdemo.resources.event_type_social_event
import com.github.mheerwaarden.eventdemo.resources.event_type_sport
import com.github.mheerwaarden.eventdemo.resources.event_type_team_building_activity
import com.github.mheerwaarden.eventdemo.resources.event_type_trade_show
import com.github.mheerwaarden.eventdemo.resources.event_type_virtual_recruiting_event
import com.github.mheerwaarden.eventdemo.resources.event_type_virtual_training_session
import com.github.mheerwaarden.eventdemo.resources.event_type_wine_experience
import com.github.mheerwaarden.eventdemo.resources.event_type_workshops
import com.github.mheerwaarden.eventdemo.ui.util.HtmlColors
import org.jetbrains.compose.resources.StringResource

enum class EventType(val text: StringResource, val htmlColor: HtmlColors) {
    ACTIVITY(Res.string.event_type_activity, HtmlColors.AQUAMARINE),
    BOAT_TRIP(Res.string.event_type_boat_trip, HtmlColors.BROWN),
    COMPANY_PARTY(Res.string.event_type_company_party, HtmlColors.BLUE),
    CONFERENCE(Res.string.event_type_conference, HtmlColors.BLUE_VIOLET),
    CORPORATE_HACKATHON(Res.string.event_type_corporate_hackathon, HtmlColors.CORAL),
    CORPORATE_OFF_SITE(Res.string.event_type_corporate_off_site, HtmlColors.CORNFLOWER_BLUE),
    CULTURAL_EXPERIENCE(Res.string.event_type_cultural_experience, HtmlColors.CRIMSON),
    EXECUTIVE_MEETING(Res.string.event_type_executive_meeting, HtmlColors.DARK_CYAN),
    EXHIBITION(Res.string.event_type_exhibition, HtmlColors.DARK_GOLDEN_ROD),
    EXPERIMENTAL_MARKETING_ACTIVATION(Res.string.event_type_experimental_marketing_activation, HtmlColors.DARK_GREEN),
    FESTIVAL(Res.string.event_type_festival, HtmlColors.DARK_OLIVE_GREEN),
    FOOD_EXPERIENCE(Res.string.event_type_food_experience, HtmlColors.DARK_ORANGE),
    FOOD_TRUCK_FESTIVAL(Res.string.event_type_food_truck_festival, HtmlColors.DARK_RED),
    GAME(Res.string.event_type_game, HtmlColors.DARK_SEA_GREEN),
    INCENTIVE(Res.string.event_type_incentive, HtmlColors.DARK_SLATE_BLUE),
    MEETING(Res.string.event_type_meeting, HtmlColors.FOREST_GREEN),
    NETWORKING(Res.string.event_type_networking, HtmlColors.GOLD),
    ONLINE_EVENT(Res.string.event_type_online_event, HtmlColors.GOLDEN_ROD),
    OUTDOOR_EXPERIENCE(Res.string.event_type_outdoor_experience, HtmlColors.INDIGO),
    PRODUCT_LAUNCH(Res.string.event_type_product_launch, HtmlColors.LIGHT_GRAY),
    SEMINAR(Res.string.event_type_seminar, HtmlColors.LIGHT_PINK),
    SOCIAL_EVENT(Res.string.event_type_social_event, HtmlColors.LIGHT_SALMON),
    SPORT(Res.string.event_type_sport, HtmlColors.LIGHT_SKY_BLUE),
    TEAM_BUILDING_ACTIVITY(Res.string.event_type_team_building_activity, HtmlColors.LIGHT_SLATE_GRAY),
    TRADE_SHOW(Res.string.event_type_trade_show, HtmlColors.MEDIUM_PURPLE),
    VIRTUAL_RECRUITING_EVENT(Res.string.event_type_virtual_recruiting_event, HtmlColors.MEDIUM_TURQUOISE),
    VIRTUAL_TRAINING_SESSION(Res.string.event_type_virtual_training_session, HtmlColors.OLIVE_DRAB),
    WINE_EXPERIENCE(Res.string.event_type_wine_experience, HtmlColors.ROYAL_BLUE),
    WORKSHOPS(Res.string.event_type_workshops, HtmlColors.SIENNA),
}

