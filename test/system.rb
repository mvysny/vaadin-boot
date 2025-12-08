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

  response.body
end

require 'pty'
require 'io/console'
require 'fileutils'

FileUtils.cd '..'
# Clean everything from any previous tests, to start at a known state.
exec './gradlew clean --no-daemon --info'

# Builds `project`, untars the runnable Vaadin-Boot archive and calls `block`.
def build_and_unzip(project, &block)
  unless File.directory? "#{project}/build"
    exec "./gradlew #{project}:build -Pvaadin.productionMode -x test --no-daemon --info"
  end
  FileUtils.cd "#{project}/build/distributions" do
    dir = Dir.glob("#{project}-*").find { File.directory? it }
    exec 'tar xvf *.tar' if dir.nil?
    dir = Dir.glob("#{project}-*").find { File.directory? it }
    raise "#{FileUtils.pwd}: tar archive didn't contain the app" if dir.nil?

    FileUtils.cd "#{dir}/bin" do
      block.call
    end
  end
end

# Builds `project`, untars the runnable Vaadin-Boot archive, runs the app and calls `block`.
def build_and_run(project, &block)
  build_and_unzip(project) do
    puts "#{project}: starting"
    PTY.spawn("./#{project}") do |reader, writer, pid|
      p = MyProc.new(pid)
      puts "#{project}: Started. Waiting 4 seconds to fully boot up"
      # Wait for the app to boot up
      sleep 4
      # Test that the app is up
      puts "#{project}: Checking the app is up"
      raise 'Not running!' unless p.running?

      puts "#{project}: App is still up"
      block.call(reader, writer, p)
    rescue StandardError => e
      puts p.read_fully(reader)
      raise e
    ensure
      writer.close
      p.kill
    end
  end
end

# Tests a project `project` {String} folder name.
def test_project(project, &block)
  # Happy test path: tests that the app runs and can be stopped via Enter
  build_and_run(project) do |_reader, writer, p|
    puts "#{project}: Checking that Vaadin is up at localhost:8080"
    body = wget('http://localhost:8080')
    raise 'Not a Vaadin index.ts' unless body.include? 'window.Vaadin'

    puts "#{project}: http://localhost:8080: OK"

    # Any optional additional testing
    block&.call

    # All's good. Now test that the app dies when Enter is pressed.
    puts "#{project}: stopping via Enter"
    writer.puts # sends Enter, VaadinBoot should quit gracefully
    p.await_shutdown
  end

  # Test that the app can be stopped via Ctrl+C
  build_and_run(project) do |_reader, _writer, p|
    # All's good. Now test that the app dies when CTRL+C is pressed.
    puts "#{project}: Stopping via CTRL+C"
    p.ctrl_c
    p.await_shutdown
  end
  puts "#{project}: OK!\n\n"
end

test_project 'testapp'
test_project 'testapp-tomcat'
test_project 'testapp-kotlin' do
  rest = wget('http://localhost:8080/rest')
  raise "Got #{rest}" unless rest == 'Hello!'

  puts 'testapp-kotlin: REST ok'
end
test_project 'testapp-kotlin-tomcat' do
  rest = wget('http://localhost:8080/rest')
  raise "Got #{rest}" unless rest == 'Hello!'

  puts 'testapp-kotlin-tomcat: REST ok'
end
