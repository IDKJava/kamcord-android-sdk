
#ifndef __KAMCORD_C_INTERFACE_H__
#define __KAMCORD_C_INTERFACE_H__

#ifdef __cplusplus
extern "C" {
#endif

#if __ANDROID__

#include <stdbool.h>
#include <jni.h>

typedef enum KC_VIDEO_QUALITY
{
    KC_STANDARD_VIDEO_QUALITY = 0,
    KC_TRAILER_VIDEO_QUALITY = 1,  // Trailers only. Do not release your game with this setting.
} KC_VIDEO_QUALITY;

typedef enum KC_METADATA_TYPE
{
    KC_LEVEL = 0,
    KC_SCORE = 1,
    KC_LIST = 2,
    KC_OTHER = 1000
} KC_METADATA_TYPE;

/*
 *
 * Kamcord initialization. Must be called before you can start recording.
 *
 * @param   developerKey            Your Kamcord developer key.
 * @param   developerSecret         Your Kamcord developerSecret.
 * @param   appName                 The name of your application.
 * @param   KC_VIDEO_QUALITY        The quality level.  Please use
 *                                  KC_STANDARD_VIDEO_QUALITY in production.
 *
 */
void Kamcord_Init(
    const char* developerKey,
    const char* developerSecret,
    const char* appName,
    KC_VIDEO_QUALITY videoQuality);

void Kamcord_InitJVM(JavaVM* vm);

void Kamcord_InitActivity(jobject activity);

/*
 * Gets a version string from kamcord.jar.
 */
const char* Kamcord_Version();

/*
 *
 * Show the watch view, which has a feed of videos shared by other users.
 *
 */
void Kamcord_ShowWatchView();

#endif // __ANDROID__

#ifdef __cplusplus
}
#endif // __cplusplus

#endif // ifndef

