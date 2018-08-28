#ifndef JNI_MD_H 
#define JNI_MD_H

#ifndef __has_attribute
#define __has_attribute(x) 0
#endif

#define JNIEXPORT __declspec(dllexport)
#define JNIIMPORT __declspec(dllimport)
#define JNICALL

typedef int jint;
typedef long long jlong;
typedef signed char jbyte;

#endif