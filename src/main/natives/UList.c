#include "UList.h"

void ulistInit(struct UList* uList) {
    uList->head = (struct UListNode*) malloc(sizeof(struct UListNode));
    uList->tail = (struct UListNode*) malloc(sizeof(struct UListNode));
    
    uList->head->next = uList->tail;
    uList->head->prev = NULL;
    
    uList->tail->next = NULL;
    uList->tail->prev = uList->head;
}

struct UListNode* ulistCreateNode(struct AudioSource* source) {
    struct UListNode* node = (struct UListNode*) malloc(sizeof(struct UListNode));
    node->audioSource = source;
    node->next = NULL;
    node->prev = NULL;
    node->connected = false;

    return node;
}

void ulistAdd(struct UList* uList, struct UListNode* node) {
    node->prev = uList->tail->prev;
    uList->tail->prev->next = node;

    uList->tail->prev = node;
    node->next = uList->tail;

    node->connected = true;
}

void ulistRemove(struct UListNode* node) {
    node->prev->next = node->next;
    node->next->prev = node->prev;

    node->connected = false;

    // node->next = NULL;
    // node->prev = NULL;
}

jboolean ulistIsTail(struct UList* uList, struct UListNode* node) {
    return node == uList->tail;
}