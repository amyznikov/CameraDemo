LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

SPECIALISDIR  := $(ANDROID_NDK)/special-is
$(call import-add-path,$(SPECIALISDIR)/lib)
$(call import-module,ffplay)
