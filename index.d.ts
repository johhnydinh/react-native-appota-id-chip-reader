declare module 'react-native-appota-id-chip-reader' {
    interface IDChipReaderModule {
      scan(options: {
        documentNumber: string;
        dateOfBirth: string;
        dateOfExpiry: string;
        quality?: number;
      }): Promise<void>;
  
      cancel(): void;
    }
  
    function assert(statement: boolean, err?: string): void;
  
    function isDate(str: any): boolean;
  
    export const IDChipReader: IDChipReaderModule;
  
    export function startScan(options: {
      documentNumber: string;
      dateOfBirth: string;
      dateOfExpiry: string;
      quality?: number;
    }): Promise<void>;
  
    export function cancelScan(): void;
  }
  