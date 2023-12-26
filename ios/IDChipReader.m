//
//  IDChipReader.m
//  IDChipReader
//
//  Created by Dinh Hoang Khang on 07/04/2023.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(IDChipReader, NSObject)

RCT_EXTERN_METHOD(
  scan: (NSDictionary *)opts
  resolver: (RCTPromiseResolveBlock) resolve
  rejecter: (RCTPromiseRejectBlock) reject)

@end
