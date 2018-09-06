#include "UList.h"

void ulistInit() {
    UList.head = (struct UListNode*) malloc(sizeof(struct UListNode));
    UList.tail = (struct UListNode*) malloc(sizeof(struct UListNode));
    
    UList.head->next = UList.tail;
    UList.head->prev = NULL;
    
    UList.tail->next = NULL;
    UList.tail->prev = UList.head;
}

void ulistAdd(struct UListNode* node ) {
    node->prev = UList.tail->prev;
    UList.tail->prev->next = node;

    UList.tail->prev = node;
    node->next = UList.tail;
}

void ulistRemove(struct UListNode* node) {
    node->prev->next = node->next;
    node->next->prev = node->prev;

    node->next = NULL;
    node->prev = NULL;
}

jboolean ulistIsTail(struct UListNode* node) {
    return node == UList.tail;
}