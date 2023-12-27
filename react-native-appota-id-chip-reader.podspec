require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name         = "react-native-appota-id-chip-reader"
  s.version      = package['version']
  s.summary      = package['description']
  s.license      = package['license']

  s.authors      = package['author']
  s.homepage     = package['repository']['url']
  s.platform     = :ios, "13.0"

  s.source       = { :git => "https://github.com/johhnydinh/react-native-appota-id-chip-reader", :tag => "v#{s.version}" }
  s.source_files  = "ios/**/*.{h,m,swift}"

  s.dependency 'React-Core'
  s.dependency "OpenSSL-Universal", '1.1.1100'
  s.xcconfig     = { 'OTHER_LDFLAGS' => '-weak_framework CryptoKit -weak_framework CoreNFC -weak_framework CryptoTokenKit' }
end