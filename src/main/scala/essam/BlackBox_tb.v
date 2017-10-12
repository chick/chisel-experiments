module BlackBox_tb(
    input  [63:0] a,
    input  [63:0] b,
    output reg [63:0] result
);
  always @* begin
  result = a & b;
  end
endmodule
