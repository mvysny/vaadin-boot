#!/usr/bin/env ruby
# frozen_string_literal: true

require 'fileutils'
require 'English'

# Runs command, raising exception on any error.
def exec(cmd)
  puts cmd
  `#{cmd}`
  raise "#{Dir.pwd}: Command '#{cmd}' failed with #{$CHILD_STATUS.exitstatus}" if $CHILD_STATUS.exitstatus != 0
end

require 'timeout'

# Awaits for process to terminate cleanly. If it doesn't, the process is killed and an exception is raised.
def wait_for(pid, seconds = 5)
  Timeout.timeout(seconds) do
    Process.wait(pid)
    puts "#{pid}: exited cleanly"
  end
rescue Timeout::Error => e
  puts "#{pid}: timed out, terminating"
  Process.kill('TERM', pid)
  begin
    Timeout.timeout(1) do
      Process.wait(pid)
    end
  rescue Timeout::Error => e
    puts "#{pid}: timed out, killing"
    Process.kill('KILL', pid)
    Process.wait(pid)
    raise e
  end
  raise e
end

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

FileUtils.cd '..'
exec './gradlew clean'
exec './gradlew testapp:build -Pvaadin.productionMode -x test'
FileUtils.cd 'testapp/build/distributions' do
  exec 'tar xvf *.tar'
  dir = Dir.glob('testapp-*').find { File.directory? it }
  FileUtils.cd "#{dir}/bin" do
    puts './testapp'
    PTY.spawn('./testapp') do |read, write, pid|
      # Wait for the app to boot up
      sleep 4
      # Test that the app is up
      wget('http://localhost:8080')

      # All's good. Now test that the app dies when Enter is pressed.
      puts "Sending Enter"
      write.puts # sends Enter, VaadinBoot should quit gracefully
      wait_for(pid, 5)
    end
  end
end
