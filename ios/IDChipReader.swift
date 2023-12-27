//
//  IDChipReader.swift
//  IDChipReader
//
//  Created by Dinh Hoang Khang on 06/04/2023.
//

import Foundation
@objc(IDChipReader)
@available(iOS 13, *)
class IDChipReader: NSObject {
  
  @objc static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  @objc public func show(
    _ opts: NSDictionary, 
    resolver resolve: RCTPromiseResolveBlock,
    rejecter reject: RCTPromiseRejectBlock
  ) -> Void {
    resolve(opts["documentNumber"])
  }
  @objc func scan(
    _ opts: NSDictionary,
    resolver resolve: @escaping RCTPromiseResolveBlock,
    rejecter reject: @escaping RCTPromiseRejectBlock
  ) -> Void {
    let mrzKey = IDUtils().getMRZKey( documentNumber: opts["documentNumber"] as! String, dateOfBirth: opts["dateOfBirth"] as! String, dateOfExpiry: opts["dateOfExpiry"] as! String)
    
    let customMessageHandler : (NFCViewDisplayMessage)->String? = { (displayMessage) in
      switch displayMessage {
      case .requestPresentPassport:
        return "Hold your iPhone near ID Chip"
      default:
        return nil
      }
    }

    Task {
      do {
        let id = try await PassportReader().readPassport( mrzKey: mrzKey, customDisplayMessage: customMessageHandler)
        let result = id.dumpPassportData(selectedDataGroups: [.COM, .SOD, .DG1, .DG2, .DG13, .DG14, .DG15])
        resolve(result)
      } catch {
        reject("0", error.localizedDescription, nil)
      }
    }
  }
}
