cmake_minimum_required(VERSION 3.10)
project(audio_module LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

add_library(audio_module SHARED
        audio_module.cpp
)

find_library(log-lib log)
find_library(android-lib android)
find_library(aaudio-lib aaudio)

target_include_directories(audio_module PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_link_libraries(audio_module
        ${log-lib}
        ${android-lib}
        ${aaudio-lib}
)
