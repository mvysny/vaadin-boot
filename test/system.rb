#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative 'myproc'

require 'net/http'
require 'uri'

# Sends HTTP GET from given URL. Raises an exception if anything is off.
def wget(url)
  uri = URI(url)
  response = Net::HTTP.get_response(uri)
  raise "#{url} failed: #{response.body}" unless response.code == '200'

  body = response.body
  raise 'Not a Vaadin index.ts' unless body.include? 'window.Vaadin'

  puts "#{url}: OK"
end

require 'pty'
require 'io/console'
require 'fileutils'

FileUtils.cd '..'
exec './gradlew clean --no-daemon --info'
exec './gradlew testapp:build -Pvaadin.productionMode -x test --no-daemon --info'
FileUtils.cd 'testapp/build/distributions' do
  exec 'tar xvf *.tar'
  dir = Dir.glob('testapp-*').find { File.directory? it }
  FileUtils.cd "#{dir}/bin" do
    puts './testapp'
    PTY.spawn('./testapp') do |reader, write, pid|
      p = MyProc.new(pid)
      # Wait for the app to boot up
      sleep 4
      # Test that the app is up
      raise 'Not running!' unless p.running?

      wget('http://localhost:8080')

      # All's good. Now test that the app dies when Enter is pressed.
      puts 'Sending Enter'
      write.puts # sends Enter, VaadinBoot should quit gracefully
      p.stop_cleanly
    rescue StandardError => e
      puts p.read_fully(reader)
      raise e
    ensure
      write.close
    end
  end
end
