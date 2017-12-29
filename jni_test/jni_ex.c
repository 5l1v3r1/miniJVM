//
// Created by gust on 2017/12/28.
//

#include <stdio.h>
#include "../mini_jvm/jvm/jvm.h"

int test_JniTest_getValue(Runtime *runtime, Class *clazz) {
    JNIENV *env = runtime->jnienv;
    int v = env->localvar_getInt(runtime, 0);
    printf("native test_JniTest_getValue(I)I invoked: v = %d\n", v);
    env->push_int(runtime->stack, v + 1);
    return 0;
}


static java_native_method method_test2_table[] = {
        {"test/JniTest", "getValue", "(I)I", test_JniTest_getValue},
};

void JNI_OnLoad(JNIENV *env) {
    env->native_reg_lib(&(method_test2_table[0]), sizeof(method_test2_table) / sizeof(java_native_method));
}
void JNI_OnUnload(JNIENV *env) {
}

int main(char **argv, int argc) {
    return 0;
}