# frozen_string_literal: true

require 'English'

# Runs command, raising exception on any error.
def exec(cmd)
  puts cmd
  `#{cmd}`
  raise "#{Dir.pwd}: Command '#{cmd}' failed with #{$CHILD_STATUS.exitstatus}" if $CHILD_STATUS.exitstatus != 0
end

require 'open3'

# Utility class for managing a process.
class MyProc
  # @return [MyProc] command running in the background.
  def self.start(command)
    stdin, stdout, wait_thread = Open3.popen2e(command)
    MyProc.new(stdin, stdout, wait_thread)
  end

  def initialize(stdin, stdout, wait_thread)
    @wait_thread = wait_thread
    @stdin = stdin
    @output = []
    # Reads stdout in the background. Handles {Errno::EIO} gracefully.
    Thread.new do
      while line = stdout.gets
        @output << line
      end
    rescue Errno::EIO
      # EIO = normal when child exits
    end
  end
  attr_reader :stdin

  # @return [Integer] PID
  def pid
    @wait_thread.pid
  end

  # @return [String] the stdout+stderr produced by the process so far.
  def output
    @output.join
  end

  # Checks if process is running.
  # @return [Boolean] true if running
  def running?
    @wait_thread.alive?
  end

  # Awaits for process to terminate cleanly. If it doesn't, the process is killed and an exception is raised.
  def await_shutdown(seconds = 5)
    return unless running?

    puts "#{pid}: awaiting #{seconds}s for process to shut down by itself"
    if wait(seconds)
      puts "#{pid}: exited cleanly"
    else
      puts "#{pid}: wait timed out, terminating"
      kill
      raise 'Timed out waiting for a clean shutdown'
    end
  end

  # Waits for this process to end. Exits immediately if the process is already stopped.
  # @return [Boolean] true if the process is stopped, false if it's still running.
  def wait(seconds = 1)
    !@wait_thread.join(seconds).nil?
  end

  # Sends TERM signal to the process and waits 1s. If nothing happens, process is killed.
  def kill
    return unless running?

    puts "#{pid}: killing via SIGTERM"
    Process.kill('TERM', pid)
    return if wait(1)

    puts "#{pid}: waiting for SIGTERM timed out, sending SIGKILL"
    Process.kill('KILL', pid)
    return if wait(1)

    puts "#{pid}: still running! Giving up." if running?
  end

  # Sends CTRL+C (SIGINT) to this process.
  def ctrl_c
    Process.kill('INT', pid)
  end

  def close
    @stdin.close
    kill
  end
end
