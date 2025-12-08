# frozen_string_literal: true

require 'English'

# Runs command, raising exception on any error.
def exec(cmd)
  puts cmd
  `#{cmd}`
  raise "#{Dir.pwd}: Command '#{cmd}' failed with #{$CHILD_STATUS.exitstatus}" if $CHILD_STATUS.exitstatus != 0
end

require 'timeout'

# Utility class for managing a process.
class MyProc
  def initialize(pid)
    @pid = pid
  end

  # Checks if process is running.
  # @return [Boolean] true if running
  def running?
    Process.kill(0, @pid)
    true
  rescue Errno::ERSCH
    false
  end

  # Awaits for process to terminate cleanly. If it doesn't, the process is killed and an exception is raised.
  def stop_cleanly(seconds = 5)
    return unless running?

    puts "#{@pid}: trying to kill gracefully"
    Timeout.timeout(seconds) do
      Process.wait(@pid)
      puts "#{@pid}: exited cleanly"
    end
  rescue Timeout::Error => e
    puts "#{@pid}: timed out, terminating"
    Process.kill('TERM', @pid)
    begin
      Timeout.timeout(1) do
        Process.wait(@pid)
      end
    rescue Timeout::Error => e
      puts "#{@pid}: timed out, killing"
      Process.kill('KILL', @pid)
      Process.wait(@pid)
      raise e
    end
    raise e
  end

  # Reads given IO fully and returns {String}. Handles {Errno::EIO} gracefully.
  # @return [String] stdin+stderr
  def read_fully(reader)
    output = []
    begin
      while line = reader.gets
        output << line
      end
    rescue Errno::EIO
      # EIO = normal when child exits
    end
    output.join
  end

  def ctrl_c
    Process.kill('INT', @pid)
  end
end
